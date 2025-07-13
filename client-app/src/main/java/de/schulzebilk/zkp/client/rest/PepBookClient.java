package de.schulzebilk.zkp.client.rest;

import de.schulzebilk.zkp.client.auth.FiatShamirProver;
import de.schulzebilk.zkp.core.auth.SessionState;
import de.schulzebilk.zkp.core.dto.AuthenticationDTO;
import de.schulzebilk.zkp.core.model.Book;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class PepBookClient extends PepClient {

    @Autowired
    PepAuthClient pepAuthClient;

    public Book getBookById(Long id, FiatShamirProver prover) {
        var responseEntity = restClient.get()
                .uri("/api/resource/book/" + id)
                .header("auth-user", prover.getProverId())
                .header("auth-payload", prover.getProverKey().toString())
                .retrieve()
                .onStatus(status -> status.value() == 401, (request, response) -> {
                    var headers = response.getHeaders();
                    AuthenticationDTO authenticate = new AuthenticationDTO(
                            headers.getFirst("auth-user"),
                            headers.getFirst("auth-session"),
                            headers.getFirst("auth-payload"),
                            SessionState.valueOf(headers.getFirst("auth-state")));
                    while (authenticate.sessionState() != SessionState.VERIFIED && authenticate.sessionState() != SessionState.FAILED) {
                        System.out.println("Verifier: " + authenticate);
                        authenticate = prover.handleAuthentication(authenticate);
                        System.out.println("Prover: " + authenticate);
                        authenticate = pepAuthClient.authenticate(authenticate);
                    }
                    System.out.println("Final Authentication State: " + authenticate.sessionState());

                })
                .toEntity(Book.class);
        System.out.println("Response Headers: " + responseEntity.getHeaders());
        return responseEntity.getBody();
    }

}
