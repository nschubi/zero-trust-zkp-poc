package de.schulzebilk.zkp.client;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class PepWebClientTest {

    @Autowired
    private PepWebClient pepWebClient;

    @Test
    void findPersonById() {
        Assertions.assertNotNull(pepWebClient.findPersonById(1L));
    }
}