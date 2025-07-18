package de.schulzebilk.zkp.client.rest;

import de.schulzebilk.zkp.core.dto.UserDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigInteger;

@Service
public class FiatShamirPepClient extends PepClient {
    private final static Logger LOG = LoggerFactory.getLogger(FiatShamirPepClient.class);

    public BigInteger getPublicModulus() {
        return restClient.get().uri("/auth/mod")
                .retrieve()
                .body(BigInteger.class);
    }

    public String registerProver(UserDTO userDTO) {
        return restClient.post().uri("/auth/register")
                .body(userDTO)
                .retrieve()
                .body(String.class);
    }

}
