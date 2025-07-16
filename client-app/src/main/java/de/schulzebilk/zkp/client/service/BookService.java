package de.schulzebilk.zkp.client.service;

import de.schulzebilk.zkp.client.auth.FiatShamirProver;
import de.schulzebilk.zkp.client.rest.PepEntityClient;
import de.schulzebilk.zkp.core.model.Book;
import de.schulzebilk.zkp.core.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class BookService {

    private final static Logger LOG = LoggerFactory.getLogger(BookService.class);
    private final PepEntityClient<Book> pepBookClient;
    private final String BOOK_URI = "/api/resource/book/";

    @Autowired
    public BookService(PepEntityClient<Book> pepBookClient) {
        this.pepBookClient = pepBookClient;
    }

    public Book getBookById(Long id, User user) {
        var uri = BOOK_URI + id;
        var book = pepBookClient.getSingleEntityByUri(uri, user, Book.class);
        LOG.info("Book retrieved: {}", book);
        return book;
    }
}
