package de.schulzebilk.zkp.pdp;

import de.schulzebilk.zkp.core.auth.SessionState;
import de.schulzebilk.zkp.pdp.helper.ProverClient;
import de.schulzebilk.zkp.pdp.model.Session;
import de.schulzebilk.zkp.pdp.service.FiatShamirVerifierService;
import org.junit.jupiter.api.Test;

import java.math.BigInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class FiatShamirUnitTest {

    @Test
    void testSuccessfulVerification() {
        FiatShamirVerifierService verifier = new FiatShamirVerifierService();
        String proverId = "prover_test";
        ProverClient prover = new ProverClient(proverId, verifier.getPublicMod(), "secretPassword");
        verifier.registerProver(prover.getProverId(), prover.getProverKey());
        Session session = verifier.createSession(proverId);
        session.startNewRound();

        BigInteger commitment = prover.generateCommitment(session.getSessionId());
        boolean challenge = verifier.generateChallenge(session.getSessionId(), commitment);
        BigInteger response = prover.generateResponse(session.getSessionId(), challenge);
        boolean isValid = verifier.verifyResponse(session.getSessionId(), response);

        assertTrue(isValid, "Verification should be successful.");
        assertEquals(SessionState.COMPLETED, session.getState());
    }

    @Test
    void testFailedVerificationWithWrongResponse() {
        FiatShamirVerifierService verifier = new FiatShamirVerifierService();
        String proverId = "prover_test2";
        ProverClient prover = new ProverClient(proverId, verifier.getPublicMod(), "secretPassword");
        verifier.registerProver(prover.getProverId(), prover.getProverKey());

        ProverClient manipulatedProverClient = new ProverClient("prover_test2", verifier.getPublicMod(), "differentPassword");

        Session session = verifier.createSession(proverId);
        while (session.getState() != SessionState.FAILED && session.getState() != SessionState.VERIFIED) {
            session.startNewRound();

            BigInteger commitment = manipulatedProverClient.generateCommitment(session.getSessionId());
            boolean challenge = verifier.generateChallenge(session.getSessionId(), commitment);
            BigInteger response = manipulatedProverClient.generateResponse(session.getSessionId(), challenge);
            boolean isValid = verifier.verifyResponse(session.getSessionId(), response);
        }

        assertEquals(SessionState.FAILED, session.getState());
    }
}

