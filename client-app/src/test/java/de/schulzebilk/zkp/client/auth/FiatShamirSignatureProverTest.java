package de.schulzebilk.zkp.client.auth;

import de.schulzebilk.zkp.client.rest.FiatShamirPepClient;
import de.schulzebilk.zkp.core.auth.AuthType;
import de.schulzebilk.zkp.core.dto.SignatureDTO;
import de.schulzebilk.zkp.core.model.User;
import de.schulzebilk.zkp.core.util.PasswordUtils;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.math.BigInteger;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@SpringBootTest
public class FiatShamirSignatureProverTest {

    @MockitoBean
    private FiatShamirPepClient fiatShamirPepClient;

    @MockitoBean
    private FiatShamirProver fiatShamirProver;

    private final BigInteger publicMod = BigInteger.valueOf(2892418973L);

    @Autowired
    private FiatShamirSignatureProver signatureProver;

    @Test
    void testGenerateSignature() {
        when(fiatShamirProver.getPublicMod()).thenReturn(publicMod);
        String sessionId = "testSession";
        User user = new User("testUser", "testSecret", AuthType.SIGNATURE);
        int rounds = 5;

        when(fiatShamirProver.calculateProverKey(user.getSecret())).thenReturn(calculateProverKey(user.getSecret()));
        SignatureDTO signature = signatureProver.generateSignature(sessionId, user, rounds);

        assertNotNull(signature);
        assertEquals(user.getUsername() + sessionId, signature.message());
        assertEquals(rounds, signature.commitments().length);
        assertEquals(rounds, signature.responses().length);
        assertTrue(signatureProver.checkSignature(signature, user.getSecret()), "Signature verification failed");
    }


    private BigInteger calculateProverKey(String password) {
        var secret = PasswordUtils.convertPasswordToBigInteger(password, publicMod);
        return secret.pow(2).mod(publicMod);
    }
}
