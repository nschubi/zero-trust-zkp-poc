package de.schulzebilk.zkp.pdp;

import de.schulzebilk.zkp.core.auth.SessionState;
import de.schulzebilk.zkp.pdp.helper.ProverClient;
import de.schulzebilk.zkp.pdp.model.Session;
import de.schulzebilk.zkp.pdp.service.VerifierService;
import org.junit.jupiter.api.Test;

import java.math.BigInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class FiatShamirUnitTest {

    @Test
    void testSuccessfulVerification() {
        VerifierService verifierService = new VerifierService();
        String proverId = "prover_test";
        ProverClient proverClient = new ProverClient(proverId, verifierService.getPublicMod(), "secretPassword");
        verifierService.registerProver(proverClient.getProverId(), proverClient.getProverKey());
        Session session = verifierService.createSession(proverId);
        session.startNewRound();

        BigInteger commitment = proverClient.generateCommitment(session.getSessionId());
        boolean challenge = verifierService.generateChallenge(session.getSessionId(), commitment);
        BigInteger response = proverClient.generateResponse(session.getSessionId(), challenge);
        boolean isValid = verifierService.verifyResponse(session.getSessionId(), response);

        assertTrue(isValid, "Verification should be successful.");
        assertEquals(SessionState.COMPLETED, session.getState());
    }

    @Test
    void testFailedVerificationWithWrongResponse() {
        VerifierService verifierService = new VerifierService();
        String proverId = "prover_test2";
        ProverClient proverClient = new ProverClient(proverId, verifierService.getPublicMod(), "secretPassword");
        verifierService.registerProver(proverClient.getProverId(), proverClient.getProverKey());

        ProverClient manipulatedProverClient = new ProverClient("prover_test2", verifierService.getPublicMod(), "differentPassword");

        Session session = verifierService.createSession(proverId);
        while (session.getState() != SessionState.FAILED && session.getState() != SessionState.VERIFIED) {
            session.startNewRound();

            BigInteger commitment = manipulatedProverClient.generateCommitment(session.getSessionId());
            boolean challenge = verifierService.generateChallenge(session.getSessionId(), commitment);
            BigInteger response = manipulatedProverClient.generateResponse(session.getSessionId(), challenge);
            boolean isValid = verifierService.verifyResponse(session.getSessionId(), response);
        }

        assertEquals(SessionState.FAILED, session.getState());
    }
}

