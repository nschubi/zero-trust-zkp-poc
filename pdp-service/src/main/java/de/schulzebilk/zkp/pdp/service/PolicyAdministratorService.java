package de.schulzebilk.zkp.pdp.service;

import de.schulzebilk.zkp.core.auth.AuthType;
import de.schulzebilk.zkp.core.auth.SessionState;
import de.schulzebilk.zkp.core.dto.AuthenticationDTO;
import de.schulzebilk.zkp.core.dto.InitialAuthenticationDTO;
import de.schulzebilk.zkp.core.dto.SignatureAuthDTO;
import de.schulzebilk.zkp.core.model.Signature;
import de.schulzebilk.zkp.core.model.User;
import de.schulzebilk.zkp.core.util.PasswordUtils;
import de.schulzebilk.zkp.pdp.model.FiatShamirSession;
import de.schulzebilk.zkp.pdp.model.PasswordSession;
import de.schulzebilk.zkp.pdp.model.Session;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigInteger;
import java.util.Objects;

@Service
public class PolicyAdministratorService {

    private final static Logger LOG = LoggerFactory.getLogger(PolicyAdministratorService.class);
    private final FiatShamirVerifierService fiatShamirVerifierService;
    private final PasswordService passwordService;
    private final PolicyEngineService policyEngineService;

    @Autowired
    public PolicyAdministratorService(FiatShamirVerifierService fiatShamirVerifierService,
                                      PasswordService passwordService,
                                      PolicyEngineService policyEngineService) {
        this.fiatShamirVerifierService = fiatShamirVerifierService;
        this.passwordService = passwordService;
        this.policyEngineService = policyEngineService;
    }

    @PostConstruct
    public void init() {
        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(
                        Objects.requireNonNull(getClass().getClassLoader().getResourceAsStream("users.csv"))))) {
            String line = br.readLine();
            var publicMod = fiatShamirVerifierService.getPublicMod();
            while ((line = br.readLine()) != null) {
                String[] fields = line.split(";");
                User user = new User(fields[0], fields[1], AuthType.valueOf(fields[2]));
                switch (user.getAuthType()) {
                    case FIATSHAMIR, SIGNATURE -> {
                        if (user.getSecret() == null || user.getSecret().isEmpty()) {
                            throw new IllegalArgumentException("Secret must not be null or empty for Fiat-Shamir authentication. User: " + user.getUsername());
                        }
                        BigInteger secret = PasswordUtils.convertPasswordToBigInteger(user.getSecret(), publicMod);
                        var proverKey = secret.pow(2).mod(publicMod);
                        registerUser(user.getUsername(), proverKey.toString(), user.getAuthType());
                    }
                    case PASSWORD -> {
                        if (user.getSecret() == null || user.getSecret().isEmpty()) {
                            throw new IllegalArgumentException("Password must not be null or empty for password authentication. User: " + user.getUsername());
                        }
                        var hash = PasswordUtils.calcualtePasswordHash(user.getSecret());
                        registerUser(user.getUsername(), hash, user.getAuthType());
                    }
                    default -> throw new IllegalArgumentException("Unknown authentication type: " + user.getAuthType());
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public AuthenticationDTO initiateAuthentication(InitialAuthenticationDTO initialAuth) {
        var auth = initialAuth.authenticationDTO();
        var authType = AuthType.valueOf(auth.sessionId());
        switch (authType) {
            case FIATSHAMIR, SIGNATURE -> {
                if (auth.proverId() == null) {
                    throw new IllegalArgumentException("Prover ID must not be null for Fiat-Shamir authentication.");
                }
                if (auth.sessionId() != null) {
                    FiatShamirSession existingSession = fiatShamirVerifierService.getSession(auth.sessionId());
                    if (existingSession != null) {
                        throw new IllegalArgumentException("Session ID already exists: " + auth.sessionId());
                    }
                }
                FiatShamirSession newSession = fiatShamirVerifierService.createSession(auth.proverId(), initialAuth.endpoint(),
                        policyEngineService.trustAlgorithm(auth.proverId(), initialAuth.endpoint(),
                                HttpMethod.valueOf(initialAuth.method())));
                if (authType == AuthType.FIATSHAMIR) {
                    newSession.startNewRound();
                    return new AuthenticationDTO(newSession.getProverId(), newSession.getSessionId(), null, newSession.getState());
                } else {
                    newSession.waitForSignature();
                    return new AuthenticationDTO(newSession.getProverId(), newSession.getSessionId(), newSession.getThreshold() + "", newSession.getState());
                }
            }
            case PASSWORD -> {
                PasswordSession passwordSession = passwordService.createSession(auth.proverId());
                return new AuthenticationDTO(auth.proverId(), passwordSession.getSessionId(), null, passwordSession.getState());
            }

            default -> {
                throw new IllegalArgumentException("Unknown authentication type: " + authType);
            }
        }

    }

    public AuthenticationDTO handleAuthentication(AuthenticationDTO auth) {
        Session session = getSessionById(auth.sessionId());
        if (session == null) {
            throw new IllegalArgumentException("Session not found for ID: " + auth.sessionId());
        }
        if (!session.getUserId().equals(auth.proverId())) {
            throw new IllegalArgumentException("User ID does not match session: " + auth.proverId());
        }
        if (session.getState() != auth.sessionState()) {
            throw new IllegalArgumentException("Session state does not match: " + session.getState() + " vs " + auth.sessionState());
        }
        if (session instanceof PasswordSession passwordSession) {
            if (auth.sessionState() == SessionState.WAITING_FOR_PASSWORD) {
                passwordService.authenticateUser(auth.sessionId(), auth.payload());
                return new AuthenticationDTO(passwordSession.getUserId(), passwordSession.getSessionId(), null, passwordSession.getState());
            }else{
                throw new IllegalArgumentException("Invalid session state: " + auth.sessionState());
            }
        } else if (session instanceof FiatShamirSession fiatShamirSession) {
            switch (auth.sessionState()) {
                case WAITING_FOR_COMMITMENT -> {
                    if (auth.payload() == null) {
                        throw new IllegalArgumentException("Payload must not be null for commitment.");
                    }
                    BigInteger commitment = PasswordUtils.convertToBigInteger(auth.payload());
                    boolean challenge = fiatShamirVerifierService.generateChallenge(fiatShamirSession.getSessionId(), commitment);
                    return new AuthenticationDTO(fiatShamirSession.getProverId(), fiatShamirSession.getSessionId(), challenge ? "true" : "false", fiatShamirSession.getState());
                }
                case WAITING_FOR_RESPONSE -> {
                    if (auth.payload() == null) {
                        throw new IllegalArgumentException("Payload must not be null for response.");
                    }
                    BigInteger response = PasswordUtils.convertToBigInteger(auth.payload());
                    fiatShamirVerifierService.verifyResponse(session.getSessionId(), response);
                    if (fiatShamirSession.getState() == SessionState.COMPLETED) {
                        fiatShamirSession.startNewRound();
                    }
                    return new AuthenticationDTO(fiatShamirSession.getProverId(), fiatShamirSession.getSessionId(), null, fiatShamirSession.getState());
                }
                default -> throw new IllegalArgumentException("Invalid session state: " + auth.sessionState());
            }
        } else {
            throw new IllegalArgumentException("Unknown session type: " + session.getClass().getName());
        }
    }

    public AuthenticationDTO handleAuthentication(SignatureAuthDTO signatureAuthDTO) {
        AuthenticationDTO auth = signatureAuthDTO.authenticationDTO();
        Signature signature = signatureAuthDTO.signature();

        FiatShamirSession session = fiatShamirVerifierService.getSession(auth.sessionId());
        if (session == null) {
            throw new IllegalArgumentException("Session not found for ID: " + auth.sessionId());
        }
        if (!session.getProverId().equals(auth.proverId())) {
            throw new IllegalArgumentException("Prover ID does not match session: " + auth.proverId());
        }
        if (session.getState() != auth.sessionState()) {
            throw new IllegalArgumentException("Session state does not match: " + session.getState() + " vs " + auth.sessionState());
        }
        session.setSignature(signature);
        fiatShamirVerifierService.checkSignature(session);
        return new AuthenticationDTO(session.getProverId(), session.getSessionId(), null, session.getState());
    }

    public void registerUser(String userId, String secret, AuthType authType) {
        if (userId == null || userId.isEmpty() || secret == null || secret.isEmpty()) {
            throw new IllegalArgumentException("User ID and secret must not be null or empty. User ID: " + userId);
        }
        if (authType == null) {
            throw new IllegalArgumentException("Authentication type must not be null.");
        }
        switch (authType) {
            case FIATSHAMIR, SIGNATURE -> {
                fiatShamirVerifierService.registerProver(userId, new BigInteger(secret));
            }
            case PASSWORD -> {
                passwordService.registerUser(userId, secret);
            }
            default -> {
                throw new IllegalArgumentException("Unknown authentication type: " + authType);
            }
        }
    }

    public BigInteger getPublicMod() {
        return fiatShamirVerifierService.getPublicMod();
    }

    private Session getSessionById(String sessionId) {
        FiatShamirSession fiatShamirSession = fiatShamirVerifierService.getSession(sessionId);
        if (fiatShamirSession != null) {
            return fiatShamirSession;
        }
        return passwordService.getSession(sessionId);
    }
}
