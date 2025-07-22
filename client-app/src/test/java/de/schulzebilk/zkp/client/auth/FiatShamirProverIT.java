package de.schulzebilk.zkp.client.auth;

import de.schulzebilk.zkp.core.auth.AuthType;
import de.schulzebilk.zkp.core.model.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class FiatShamirProverIT {

    @Autowired
    private FiatShamirProver prover;

    @Test
    void testFiatShamirProver() {
        String proverId = "prover_test";
        String proverKey = "testPassword";
        User user = new User(proverId, proverKey, AuthType.FIATSHAMIR);

        String registerResponse = prover.registerProver(user);
        System.out.println("Register Response: " + registerResponse);

    }

}
