package de.schulzebilk.zkp.pep.client;

import de.schulzebilk.zkp.core.model.auth.AuthenticationRequest;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

@Service
public class PdpWebClient {

    private final static Logger LOG = LoggerFactory.getLogger(PdpWebClient.class);

    @Value("${pdp.service.url}")
    private String pdpServiceUrl;

    private WebClient webClient;

    @PostConstruct
    public void init() {
        LOG.info("PdpWebClient instantiated, service URL: {}", pdpServiceUrl);
        webClient = WebClient.create(pdpServiceUrl);
    }

    public boolean authenticate(String username, String password) {
        AuthenticationRequest request = new AuthenticationRequest(username, password);

        return Boolean.TRUE.equals(webClient
                .post()
                .uri("/auth/authenticate")
                .bodyValue(request)
                .retrieve()
                .bodyToMono(Boolean.class)
                .block());
    }


}
