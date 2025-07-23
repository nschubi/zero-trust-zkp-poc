package de.schulzebilk.zkp.pdp.service;

import de.schulzebilk.zkp.core.auth.AuthType;
import de.schulzebilk.zkp.core.auth.SessionState;
import de.schulzebilk.zkp.core.dto.AuthenticationDTO;
import de.schulzebilk.zkp.core.dto.InitialAuthenticationDTO;
import de.schulzebilk.zkp.core.model.User;
import de.schulzebilk.zkp.core.util.PasswordUtils;
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
                    case FIATSHAMIR -> {
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
        switch(authType) {
            case FIATSHAMIR -> {
                if (auth.proverId() == null) {
                    throw new IllegalArgumentException("Prover ID must not be null for Fiat-Shamir authentication.");
                }
                if (auth.sessionId() != null) {
                    Session existingSession = fiatShamirVerifierService.getSession(auth.sessionId());
                    if (existingSession != null) {
                        throw new IllegalArgumentException("Session ID already exists: " + auth.sessionId());
                    }
                }
                Session newSession = fiatShamirVerifierService.createSession(auth.proverId(), initialAuth.endpoint(),
                        policyEngineService.trustAlgorithm(auth.proverId(), initialAuth.endpoint(),
                                HttpMethod.valueOf(initialAuth.method())));
                newSession.startNewRound();
                return new AuthenticationDTO(newSession.getProverId(), newSession.getSessionId(), null, newSession.getState());
            }
            case PASSWORD ->{
                boolean isAuthenticated = passwordService.authenticateUser(auth.proverId(),auth.payload());
                if (!isAuthenticated) {
                    throw new IllegalArgumentException("Authentication failed for user: " + auth.proverId());
                }
                return new AuthenticationDTO(auth.proverId(), null, null, SessionState.VERIFIED);
            }

            default -> {
                throw new IllegalArgumentException("Unknown authentication type: " + authType);
            }
        }

    }


    public AuthenticationDTO handleAuthentication(AuthenticationDTO auth) {
        Session session = fiatShamirVerifierService.getSession(auth.sessionId());
        if (session == null) {
            throw new IllegalArgumentException("Session not found for ID: " + auth.sessionId());
        }
        if (!session.getProverId().equals(auth.proverId())) {
            throw new IllegalArgumentException("Prover ID does not match session: " + auth.proverId());
        }
        if (session.getState() != auth.sessionState()) {
            throw new IllegalArgumentException("Session state does not match: " + session.getState() + " vs " + auth.sessionState());
        }
        switch (auth.sessionState()) {
            case WAITING_FOR_COMMITMENT -> {
                if (auth.payload() == null) {
                    throw new IllegalArgumentException("Payload must not be null for commitment.");
                }
                BigInteger commitment = PasswordUtils.convertToBigInteger(auth.payload());
                boolean challenge = fiatShamirVerifierService.generateChallenge(session.getSessionId(), commitment);
                return new AuthenticationDTO(session.getProverId(), session.getSessionId(), challenge ? "true" : "false", session.getState());
            }
            case WAITING_FOR_RESPONSE -> {
                if (auth.payload() == null) {
                    throw new IllegalArgumentException("Payload must not be null for response.");
                }
                BigInteger response = PasswordUtils.convertToBigInteger(auth.payload());
                fiatShamirVerifierService.verifyResponse(session.getSessionId(), response);
                if (session.getState() == SessionState.COMPLETED) {
                    session.startNewRound();
                }
                return new AuthenticationDTO(session.getProverId(), session.getSessionId(), null, session.getState());
            }
            default -> {
                throw new IllegalArgumentException("Invalid session state: " + auth.sessionState());
            }
        }
    }

    public void registerUser(String userId, String secret, AuthType authType) {
        if (userId == null || userId.isEmpty() || secret == null || secret.isEmpty()) {
            throw new IllegalArgumentException("User ID and secret must not be null or empty. User ID: " + userId);
        }
        if (authType == null) {
            throw new IllegalArgumentException("Authentication type must not be null.");
        }
        switch (authType) {
            case FIATSHAMIR -> {
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
}
