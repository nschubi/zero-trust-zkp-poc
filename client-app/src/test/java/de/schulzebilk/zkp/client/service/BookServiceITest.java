package de.schulzebilk.zkp.client.service;

import de.schulzebilk.zkp.client.auth.FiatShamirProver;
import de.schulzebilk.zkp.client.rest.PepAuthClient;
import de.schulzebilk.zkp.core.model.Book;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
public class BookServiceITest {

    @Autowired
    private BookService bookService;

    @Autowired
    private PepAuthClient pepAuthClient;

    @Test
    void testGetBookById() {
        BigInteger publicMod = pepAuthClient.getPublicModulus();
        String proverId = "prover_test";
        String proverKey = "testPassword";
        FiatShamirProver prover = new FiatShamirProver(proverId, publicMod, proverKey);
//        pepAuthClient.registerProver(prover.getRegisterProverDTO());

        Long bookId = 1L;
        Book book = bookService.getBookById(bookId, prover);

        assertNotNull(book);
        assertEquals(bookId, book.getId());
    }
}
