package de.schulzebilk.zkp.client;

import de.schulzebilk.zkp.core.model.Person;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

@Service
public class PepWebClient {
    private final static Logger LOG = LoggerFactory.getLogger(PepWebClient.class);

    @Value("${service.url.pep}")
    private String pepServiceUrl;

    private WebClient webClient;

    @PostConstruct
    public void init() {
        webClient = WebClient.create(pepServiceUrl);
    }

    public Person findPersonById(Long id) {
        return webClient
                .get()
                .uri("/api/resource/person/" + id)
                        .header("X-Username", "Test")
                        .header("X-Password", "Test")
                .retrieve()
                .bodyToMono(Person.class)
                .block();
    }
}
