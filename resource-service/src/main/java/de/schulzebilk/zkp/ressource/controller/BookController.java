package de.schulzebilk.zkp.ressource.controller;

import de.schulzebilk.zkp.ressource.model.Book;
import de.schulzebilk.zkp.ressource.service.BookService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/book")
public class BookController {

    private final static Logger LOG = LoggerFactory.getLogger(BookController.class);
    private final BookService bookService;

    @Autowired
    public BookController(BookService bookService) {
        this.bookService = bookService;
    }

    @GetMapping
    public ResponseEntity<Iterable<Book>> getAllBooks() {
        Iterable<Book> books = bookService.findAll();
        return ResponseEntity.ok(books);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Book> getBookById(@PathVariable("id") Long id) {
        var book = bookService.findById(id);
        if (book == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(book);
    }

    @PostMapping
    public ResponseEntity<Book> createBook(@RequestBody Book book) {
        var savedBook = bookService.save(book);
        LOG.info("Book created: {}", savedBook);
        return ResponseEntity.ok(savedBook);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Book> updateBook(@PathVariable("id") Long id, @RequestBody Book book) {
        var existingBook = bookService.findById(id);
        if (existingBook == null) {
            return ResponseEntity.notFound().build();
        }
        existingBook.setTitle(book.getTitle());
        existingBook.setAuthor(book.getAuthor());
        existingBook.setPublicationYear(book.getPublicationYear());
        var updatedBook = bookService.save(existingBook);
        return ResponseEntity.ok(updatedBook);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBook(@PathVariable("id") Long id) {
        var existingBook = bookService.findById(id);
        if (existingBook == null) {
            return ResponseEntity.notFound().build();
        }
        bookService.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
