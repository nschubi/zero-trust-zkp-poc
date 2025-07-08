package de.schulzebilk.zkp.client;

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
}
