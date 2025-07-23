package de.schulzebilk.zkp.client;

import de.schulzebilk.zkp.client.rest.FiatShamirPepClient;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.math.BigInteger;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@SpringBootTest
class ClientApplicationTest {

    @MockitoBean
    private FiatShamirPepClient fiatShamirPepClient;

    @Test
    void testStart() {
        when(fiatShamirPepClient.getPublicModulus()).thenReturn(BigInteger.valueOf(2892418973L));
        assertTrue(true);
    }

}