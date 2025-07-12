package de.schulzebilk.zkp.client.service;

import de.schulzebilk.zkp.client.rest.PepBookClient;
import de.schulzebilk.zkp.core.model.Book;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class BookService {

    private final static Logger LOG = LoggerFactory.getLogger(BookService.class);
    private final PepBookClient pepBookClient;

    @Autowired
    public BookService(PepBookClient pepBookClient) {
        this.pepBookClient = pepBookClient;
    }

    public Book getBookById(Long id) {
        var book = pepBookClient.getBookById(id);
        LOG.info("Book retrieved: {}", book);
        return book;

    }
}
