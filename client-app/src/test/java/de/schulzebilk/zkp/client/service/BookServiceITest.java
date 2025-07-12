package de.schulzebilk.zkp.client.service;

import de.schulzebilk.zkp.core.model.Book;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
public class BookServiceITest {

    @Autowired
    private BookService bookService;

    @Test
    void testGetBookById() {
        Long bookId = 1L;
        Book book = bookService.getBookById(bookId);

        assertNotNull(book);
        assertEquals(bookId, book.getId());
    }
}
