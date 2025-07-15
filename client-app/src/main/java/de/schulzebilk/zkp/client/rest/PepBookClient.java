package de.schulzebilk.zkp.client.rest;

import de.schulzebilk.zkp.client.auth.FiatShamirProver;
import de.schulzebilk.zkp.core.auth.SessionState;
import de.schulzebilk.zkp.core.dto.AuthenticationDTO;
import de.schulzebilk.zkp.core.model.Book;
import de.schulzebilk.zkp.core.util.AuthUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@Component
public class PepBookClient extends PepClient {

    public Book getBookById(Long id, FiatShamirProver prover) {
        var responseEntity = restClient.get()
                .uri("/api/resource/book/" + id)
                .header("auth-user", prover.getProverId())
                .header("auth-payload", prover.getProverKey().toString())
                .retrieve()
                .onStatus(status -> status.value() == 401, (request, response) -> {
                    AuthenticationDTO authenticate = AuthUtils.createAuthenticationDtoFromHeaders(response.getHeaders());
                    ResponseEntity<?> authResponse = null;
                    while (authenticate.sessionState() != SessionState.VERIFIED
                            && authenticate.sessionState() != SessionState.FAILED) {
                        System.out.println("Verifier: " + authenticate);
                        authenticate = prover.handleAuthentication(authenticate);
                        System.out.println("Prover: " + authenticate);
                        authResponse = sendAuthentication(authenticate);
                        System.out.println("Headers from Auth Response: " + authResponse.getHeaders());
                        authenticate = AuthUtils.createAuthenticationDtoFromHeaders(authResponse.getHeaders());
                    }
                    System.out.println("Final Authentication State: " + authenticate.sessionState());
                    Book book = (Book) authResponse.getBody();
                    System.out.println(book);
                })
                .toEntity(Book.class);
        System.out.println("Response Headers: " + responseEntity.getHeaders());
        return responseEntity.getBody();
    }

    public ResponseEntity<?> sendAuthentication(AuthenticationDTO authenticationDTO) {
       return restClient.post().uri("/api/authenticate")
                .body(authenticationDTO)
                .retrieve()
                .toEntity(Book.class);
    }
}
