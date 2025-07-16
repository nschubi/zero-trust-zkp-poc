package de.schulzebilk.zkp.client.rest;

import de.schulzebilk.zkp.client.auth.FiatShamirProver;
import de.schulzebilk.zkp.core.auth.SessionState;
import de.schulzebilk.zkp.core.dto.AuthenticationDTO;
import de.schulzebilk.zkp.core.util.AuthUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@Component
public class PepEntityClient<T> extends PepClient {

    @SuppressWarnings("unchecked")
    public T getSingleEntityByUri(String uri, FiatShamirProver prover, Class<?> clazz) {
        ResponseEntity<?> response = restClient.get()
                .uri(uri)
                .header("auth-user", prover.getProverId())
                .header("auth-payload", prover.getProverKey().toString())
                .retrieve()
                .toEntity(clazz);

        AuthenticationDTO authenticate = AuthUtils.createAuthenticationDtoFromHeaders(response.getHeaders());
        while (authenticate.sessionState() != SessionState.VERIFIED
                && authenticate.sessionState() != SessionState.FAILED) {
            authenticate = prover.handleAuthentication(authenticate);
            response = sendAuthentication(authenticate,clazz);
            authenticate = AuthUtils.createAuthenticationDtoFromHeaders(response.getHeaders());
        }

        if (response.getStatusCode().is2xxSuccessful() && authenticate.sessionState() == SessionState.VERIFIED) {
            return (T) response.getBody();
        } else {
            throw new RuntimeException("Failed to retrieve entity from URI: " + uri);
        }
    }

    private ResponseEntity<?> sendAuthentication(AuthenticationDTO authenticationDTO, Class<?> clazz) {
        return restClient.post().uri("/api/authenticate")
                .body(authenticationDTO)
                .retrieve()
                .toEntity(clazz);
    }
}
