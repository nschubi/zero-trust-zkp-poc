package de.schulzebilk.zkp.client.auth;

import de.schulzebilk.zkp.client.rest.PepAuthClient;
import de.schulzebilk.zkp.core.auth.SessionState;
import de.schulzebilk.zkp.core.dto.AuthenticationDTO;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigInteger;

@SpringBootTest
public class FiatShamirProverITest {

    @Autowired
    private PepAuthClient pepAuthClient;


    @Test
    void testFiatShamirProver() {
        BigInteger publicMod = pepAuthClient.getPublicModulus();
        String proverId = "prover_test";
        String proverKey = "testPassword";
        FiatShamirProver prover = new FiatShamirProver(proverId, publicMod, proverKey);

        // Register the prover
        String registerResponse = pepAuthClient.registerProver(prover.getRegisterProverDTO());
        System.out.println("Register Response: " + registerResponse);

        // Initial authentication message
        AuthenticationDTO auth = new AuthenticationDTO(proverId, null, "resource/book/1", null);
        AuthenticationDTO response = pepAuthClient.authenticate(auth);
        while (response.sessionState() != SessionState.VERIFIED && response.sessionState() != SessionState.FAILED) {
            System.out.println("Verifier: " + response);
            response = prover.handleAuthentication(response);
            System.out.println("Prover: " + response);
            response = pepAuthClient.authenticate(response);
        }
    }

}
