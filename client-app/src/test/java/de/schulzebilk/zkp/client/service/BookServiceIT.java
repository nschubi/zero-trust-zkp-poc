package de.schulzebilk.zkp.client.service;

import de.schulzebilk.zkp.core.auth.AuthType;
import de.schulzebilk.zkp.core.model.Book;
import de.schulzebilk.zkp.core.model.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
public class BookServiceIT {

    @Autowired
    private BookService bookService;

    @Test
    void testGetBookById() {
        String proverId = "alice";
        String proverKey = "password123";
        User user = new User(proverId, proverKey, AuthType.FIATSHAMIR);

        Long bookId = 1L;
        Book book = bookService.getBookById(bookId, user);

        assertNotNull(book);
        assertEquals(bookId, book.getId());
    }


    @Test
    void testCreateBook() {
        String proverId = "alice";
        String proverKey = "password123";
        User user = new User(proverId, proverKey, AuthType.FIATSHAMIR);

        Book book = new Book("Testbook", "Test Author", 2025);

        Book createdBook = bookService.createBook(book, user);

        assertNotNull(createdBook);
        System.out.println("Created Book: " + createdBook);
    }

}
