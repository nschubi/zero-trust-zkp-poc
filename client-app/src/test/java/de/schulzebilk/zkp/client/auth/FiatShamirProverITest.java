package de.schulzebilk.zkp.client.auth;

import de.schulzebilk.zkp.client.PepWebClient;
import de.schulzebilk.zkp.core.auth.SessionState;
import de.schulzebilk.zkp.core.dto.AuthenticationDTO;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigInteger;

@SpringBootTest
public class FiatShamirProverITest {

    @Autowired
    private PepWebClient pepWebClient;


    @Test
    void testFiatShamirProver() {
        BigInteger publicMod = pepWebClient.getPublicModulus();
        String proverId = "prover_test";
        String proverKey = "testPassword";
        FiatShamirProver prover = new FiatShamirProver(proverId, publicMod, proverKey);

        // Register the prover
        String registerResponse = pepWebClient.registerProver(prover.getRegisterProverDTO());
        System.out.println("Register Response: " + registerResponse);

        // Initial authentication message
        AuthenticationDTO auth = new AuthenticationDTO(proverId, null, "resource/book/1", null);
        AuthenticationDTO response = pepWebClient.authenticate(auth);
        while (response.sessionState() != SessionState.VERIFIED && response.sessionState() != SessionState.FAILED) {
            System.out.println("Verifier: " + response);
            response = prover.handleAuthentication(response);
            System.out.println("Prover: " + response);
            response = pepWebClient.authenticate(response);
        }
    }

}
