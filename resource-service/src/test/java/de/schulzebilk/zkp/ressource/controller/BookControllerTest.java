package de.schulzebilk.zkp.ressource.controller;

import de.schulzebilk.zkp.ressource.model.Book;
import de.schulzebilk.zkp.ressource.service.BookService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class BookControllerTest {

    @Mock
    private BookService bookService;

    @InjectMocks
    private BookController bookController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void getAllBooks() {
        List<Book> books = Arrays.asList(
            new Book("Dune", "Frank Herbert", 1965),
            new Book("The Hobbit", "J.R.R. Tolkien", 1937)
        );
        when(bookService.findAll()).thenReturn(books);

        ResponseEntity<Iterable<Book>> response = bookController.getAllBooks();

        assertEquals(200, response.getStatusCode().value());
        assertEquals(books, response.getBody());
        verify(bookService).findAll();
    }

    @Test
    void getBookById_found() {
        Book book = new Book("Dune", "Frank Herbert", 1965);
        when(bookService.findById(1L)).thenReturn(book);

        ResponseEntity<Book> response = bookController.getBookById(1L);

        assertEquals(200, response.getStatusCode().value());
        assertEquals(book, response.getBody());
        verify(bookService).findById(1L);
    }

    @Test
    void getBookById_notFound() {
        when(bookService.findById(2L)).thenReturn(null);

        ResponseEntity<Book> response = bookController.getBookById(2L);

        assertEquals(404, response.getStatusCode().value());
        assertNull(response.getBody());
        verify(bookService).findById(2L);
    }

    @Test
    void createBook() {
        Book book = new Book("New Book", "New Author", 2024);
        Book savedBook = new Book("New Book", "New Author", 2024);
        when(bookService.save(book)).thenReturn(savedBook);

        ResponseEntity<Book> response = bookController.createBook(book);

        assertEquals(200, response.getStatusCode().value());
        assertEquals(savedBook, response.getBody());
        verify(bookService).save(book);
    }

    @Test
    void updateBook_found() {
        Book existingBook = new Book("Old Title", "Old Author", 2000);
        Book updatedBook = new Book("New Title", "New Author", 2024);
        when(bookService.findById(1L)).thenReturn(existingBook);
        when(bookService.save(existingBook)).thenReturn(existingBook);

        ResponseEntity<Book> response = bookController.updateBook(1L, updatedBook);

        assertEquals(200, response.getStatusCode().value());
        assertEquals(existingBook, response.getBody());
        assertEquals("New Title", existingBook.getTitle());
        assertEquals("New Author", existingBook.getAuthor());
        assertEquals(2024, existingBook.getPublicationYear());
        verify(bookService).findById(1L);
        verify(bookService).save(existingBook);
    }

    @Test
    void updateBook_notFound() {
        Book updatedBook = new Book("New Title", "New Author", 2024);
        when(bookService.findById(2L)).thenReturn(null);

        ResponseEntity<Book> response = bookController.updateBook(2L, updatedBook);

        assertEquals(404, response.getStatusCode().value());
        assertNull(response.getBody());
        verify(bookService).findById(2L);
        verify(bookService, never()).save(any());
    }

    @Test
    void deleteBook_found() {
        Book existingBook = new Book("Book to Delete", "Author", 2000);
        when(bookService.findById(1L)).thenReturn(existingBook);

        ResponseEntity<Void> response = bookController.deleteBook(1L);

        assertEquals(204, response.getStatusCode().value());
        assertNull(response.getBody());
        verify(bookService).findById(1L);
        verify(bookService).deleteById(1L);
    }

    @Test
    void deleteBook_notFound() {
        when(bookService.findById(2L)).thenReturn(null);

        ResponseEntity<Void> response = bookController.deleteBook(2L);

        assertEquals(404, response.getStatusCode().value());
        assertNull(response.getBody());
        verify(bookService).findById(2L);
        verify(bookService, never()).deleteById(any());
    }
}