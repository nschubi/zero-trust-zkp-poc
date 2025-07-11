package de.schulzebilk.zkp.client;

import de.schulzebilk.zkp.core.dto.AuthenticationDTO;
import de.schulzebilk.zkp.core.dto.RegisterProverDTO;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.math.BigInteger;

@Service
public class PepWebClient {
    private final static Logger LOG = LoggerFactory.getLogger(PepWebClient.class);

    @Value("${service.url.pep}")
    private String pepServiceUrl;

    private RestClient restClient;

    @PostConstruct
    public void init() {
        restClient = RestClient.create(pepServiceUrl);
    }

    public BigInteger getPublicModulus() {
        return restClient.get().uri("/auth/mod")
                .retrieve()
                .body(BigInteger.class);
    }

    public String registerProver(RegisterProverDTO registerProverDTO) {
        return restClient.post().uri("/auth/register")
                .body(registerProverDTO)
                .retrieve()
                .body(String.class);
    }

    public AuthenticationDTO authenticate(AuthenticationDTO authenticationDTO) {
        return restClient.post().uri("/auth/authenticate")
                .body(authenticationDTO)
                .retrieve()
                .body(AuthenticationDTO.class);
    }
}
