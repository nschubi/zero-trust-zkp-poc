package de.schulzebilk.zkp.client.rest;

import de.schulzebilk.zkp.core.dto.AuthenticationDTO;
import de.schulzebilk.zkp.core.dto.RegisterProverDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigInteger;

@Service
public class PepAuthClient extends PepClient {
    private final static Logger LOG = LoggerFactory.getLogger(PepAuthClient.class);

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
