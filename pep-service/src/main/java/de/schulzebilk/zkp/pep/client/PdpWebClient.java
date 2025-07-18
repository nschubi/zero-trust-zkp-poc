package de.schulzebilk.zkp.pep.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.schulzebilk.zkp.core.dto.AuthenticationDTO;
import de.schulzebilk.zkp.core.dto.InitialAuthenticationDTO;
import de.schulzebilk.zkp.core.dto.RegisterProverDTO;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.math.BigInteger;

@Service
public class PdpWebClient {

    private final static Logger LOG = LoggerFactory.getLogger(PdpWebClient.class);

    @Value("${service.url.pdp}")
    private String pdpServiceUrl;

    private RestClient restClient;

    @PostConstruct
    public void init() {
        restClient = RestClient.create(pdpServiceUrl);
    }

    public BigInteger getPublicModulus() {
        return restClient.get().uri("/mod")
                .retrieve()
                .body(BigInteger.class);
    }

    public String registerProver(RegisterProverDTO registerProverDTO) {
        return restClient.post().uri("/register")
                .body(registerProverDTO)
                .retrieve()
                .body(String.class);
    }

    public AuthenticationDTO initiateAuthentication(InitialAuthenticationDTO authenticationDTO) {
        ObjectMapper mapper = new ObjectMapper();
        try {
            String json = mapper.writeValueAsString(authenticationDTO);
            System.out.println("Sending JSON: " + json);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return restClient.post().uri("/initiate")
                .body(authenticationDTO)
                .retrieve()
                .body(AuthenticationDTO.class);
    }

    public AuthenticationDTO authenticate(AuthenticationDTO authenticationDTO) {
        return restClient.post().uri("/authenticate")
                .body(authenticationDTO)
                .retrieve()
                .body(AuthenticationDTO.class);
    }

}
