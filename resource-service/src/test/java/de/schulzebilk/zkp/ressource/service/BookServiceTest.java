package de.schulzebilk.zkp.ressource.service;

import de.schulzebilk.zkp.ressource.model.Book;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class BookServiceTest {

    @Autowired
    private BookService bookService;

    @Test
    void findAll() {
        var books = bookService.findAll();
        assertNotNull(books, "Expected books list to not be null");
        assertTrue(books.iterator().hasNext(), "Expected at least one book in the list");
    }

    @Test
    void findById() {
        var book = bookService.findById(1L);
        assertNotNull(book, "Expected a book with ID 1 to be found");
        assertEquals(1L, book.getId(), "Expected the book ID to be 1");

        var nonExistentBook = bookService.findById(999L);
        assertNull(nonExistentBook, "Expected no book to be found with ID 999");
    }

    @Test
    void save() {
        var book = new Book("Test Book", "Test Author", 2025);
        var savedBook = bookService.save(book);
        assertNotNull(savedBook, "Expected the saved book to not be null");
        assertEquals("Test Book", savedBook.getTitle(), "Expected the book title to be 'Test Book'");
        assertEquals("Test Author", savedBook.getAuthor(), "Expected the book author to be 'Test Author'");
        assertEquals(2025, savedBook.getPublicationYear(), "Expected the book year to be 2025");
    }

    @Test
    void deleteById() {
        var book = new Book("Delete Me", "Author", 2025);
        var savedBook = bookService.save(book);
        assertNotNull(savedBook, "Expected the book to be saved successfully");

        bookService.deleteById(savedBook.getId());
        var deletedBook = bookService.findById(savedBook.getId());
        assertNull(deletedBook, "Expected the book to be deleted and not found");
    }
}