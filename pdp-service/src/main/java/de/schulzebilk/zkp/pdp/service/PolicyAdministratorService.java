package de.schulzebilk.zkp.pdp.service;

import de.schulzebilk.zkp.core.dto.AuthenticationDTO;
import de.schulzebilk.zkp.core.auth.SessionState;
import de.schulzebilk.zkp.core.util.PasswordUtils;
import de.schulzebilk.zkp.pdp.model.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigInteger;

@Service
public class PolicyAdministratorService {

    private final FiatShamirVerifierService fiatShamirVerifierService;
    private final PolicyEngineService policyEngineService;

    @Autowired
    public PolicyAdministratorService(FiatShamirVerifierService fiatShamirVerifierService, PolicyEngineService policyEngineService) {
        this.fiatShamirVerifierService = fiatShamirVerifierService;
        this.policyEngineService = policyEngineService;
    }

    public AuthenticationDTO handleAuthentication(AuthenticationDTO auth) {
        if (auth.sessionState() == null) {
            if (auth.proverId() == null || auth.payload() == null) {
                throw new IllegalArgumentException("Prover ID and payload must not be null. Provider ID: " + auth.proverId() + ", Payload: " + auth.payload());
            }
            if (auth.sessionId() != null) {
                Session existingSession = fiatShamirVerifierService.getSession(auth.sessionId());
                if (existingSession != null) {
                    throw new IllegalArgumentException("Session ID already exists: " + auth.sessionId());
                }
            }
            Session newSession = fiatShamirVerifierService.createSession(auth.proverId(), auth.payload(),
                    policyEngineService.trustAlgorithm(auth.proverId(), auth.payload()));
            newSession.startNewRound();
            return new AuthenticationDTO(newSession.getProverId(), newSession.getSessionId(), null, newSession.getState());
        }

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

}
