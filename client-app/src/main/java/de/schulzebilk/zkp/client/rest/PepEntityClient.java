package de.schulzebilk.zkp.client.rest;

import de.schulzebilk.zkp.client.auth.FiatShamirProver;
import de.schulzebilk.zkp.core.auth.SessionState;
import de.schulzebilk.zkp.core.dto.AuthenticationDTO;
import de.schulzebilk.zkp.core.model.User;
import de.schulzebilk.zkp.core.util.AuthUtils;
import de.schulzebilk.zkp.core.util.PasswordUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@Component
public class PepEntityClient<T> extends PepClient {

    private final FiatShamirProver prover;

    @Autowired
    public PepEntityClient(FiatShamirProver prover) {
        this.prover = prover;
    }


    @SuppressWarnings("unchecked")
    public T getSingleEntityByUri(String uri, User user, Class<T> clazz) {
        ResponseEntity<?> response = restClient.get()
                .uri(uri)
                .header("auth-user", user.getUsername())
                .header("auth-payload", getSecretForUser(user) == null ? "" : getSecretForUser(user))
                .header("auth-session", user.getAuthType().toString())
                .retrieve()
                .toEntity(clazz);

        if (response.getHeaders().containsKey("auth-state")) {
            AuthenticationDTO authenticate = AuthUtils.createAuthenticationDtoFromHeaders(response.getHeaders());
            while (authenticate.sessionState() != SessionState.VERIFIED
                    && authenticate.sessionState() != SessionState.FAILED) {
                authenticate = prover.handleAuthentication(user, authenticate);
                response = sendAuthentication(authenticate, clazz);
                authenticate = AuthUtils.createAuthenticationDtoFromHeaders(response.getHeaders());
            }

            if (response.getStatusCode().is2xxSuccessful() && authenticate.sessionState() == SessionState.VERIFIED) {
                return (T) response.getBody();
            } else {
                throw new RuntimeException("Failed to retrieve entity from URI: " + uri);
            }

        }
        return (T) response.getBody();
    }

    public T createEntity(String uri, T entity, User user, Class<T> clazz) {
        ResponseEntity<?> response = restClient.post()
                .uri(uri)
                .header("auth-user", user.getUsername())
                .header("auth-payload", getSecretForUser(user))
                .header("auth-session", user.getAuthType().toString())
                .body(entity)
                .retrieve()
                .toEntity(clazz);

        AuthenticationDTO authenticate = AuthUtils.createAuthenticationDtoFromHeaders(response.getHeaders());
        while (authenticate.sessionState() != SessionState.VERIFIED
                && authenticate.sessionState() != SessionState.FAILED) {
            authenticate = prover.handleAuthentication(user, authenticate);
            response = sendAuthentication(authenticate, clazz);
            authenticate = AuthUtils.createAuthenticationDtoFromHeaders(response.getHeaders());
        }

        if (response.getStatusCode().is2xxSuccessful() && authenticate.sessionState() == SessionState.VERIFIED) {
            return (T) response.getBody();
        } else {
            throw new RuntimeException("Failed to retrieve entity from URI: " + uri);
        }
    }

    private ResponseEntity<?> sendAuthentication(AuthenticationDTO authenticationDTO, Class<T> clazz) {
        return restClient.post().uri("/api/authenticate")
                .body(authenticationDTO)
                .retrieve()
                .toEntity(clazz);
    }

    private String getSecretForUser(User user) {
        switch (user.getAuthType()) {
            case FIATSHAMIR -> {
                return null;
            }
            case PASSWORD -> {
                return PasswordUtils.calcualteHash(user.getSecret());
            }
            default -> {
                throw new IllegalArgumentException("Unknown authentication type: " + user.getAuthType());
            }
        }
    }
}
