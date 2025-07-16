package de.schulzebilk.zkp.client.service;

import de.schulzebilk.zkp.client.auth.FiatShamirProver;
import de.schulzebilk.zkp.core.auth.AuthType;
import de.schulzebilk.zkp.core.model.Book;
import de.schulzebilk.zkp.core.model.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
public class BookServiceITest {

    @Autowired
    private BookService bookService;

    @Autowired
    private FiatShamirProver prover;

    @Test
    void testGetBookById() {
        String proverId = "prover_test";
        String proverKey = "testPassword";
        User user = new User(proverId, proverKey, AuthType.FIATSHAMIR);

//        prover.registerProver(user);

        Long bookId = 1L;
        Book book = bookService.getBookById(bookId, user);

        assertNotNull(book);
        assertEquals(bookId, book.getId());
    }
}
