package de.schulzebilk.zkp.client.rest;

import de.schulzebilk.zkp.core.model.Book;
import org.springframework.stereotype.Component;

@Component
public class PepBookClient extends PepClient {

    public Book getBookById(Long id) {
        return restClient.get().uri("/api/resource/book/" + id)
                .retrieve()
                .body(Book.class);
    }

}
