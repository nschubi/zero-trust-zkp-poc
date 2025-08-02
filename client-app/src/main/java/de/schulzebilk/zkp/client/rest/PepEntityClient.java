package de.schulzebilk.zkp.client.rest;

import de.schulzebilk.zkp.client.auth.FiatShamirProver;
import de.schulzebilk.zkp.client.auth.FiatShamirSignatureProver;
import de.schulzebilk.zkp.core.auth.AuthType;
import de.schulzebilk.zkp.core.auth.SessionState;
import de.schulzebilk.zkp.core.dto.AuthenticationDTO;
import de.schulzebilk.zkp.core.dto.SignatureAuthDTO;
import de.schulzebilk.zkp.core.model.Signature;
import de.schulzebilk.zkp.core.model.User;
import de.schulzebilk.zkp.core.util.AuthUtils;
import de.schulzebilk.zkp.core.util.PasswordUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@Component
public class PepEntityClient<T> extends PepClient {

    private final FiatShamirProver prover;
    private final FiatShamirSignatureProver signatureProver;


    @Autowired
    public PepEntityClient(FiatShamirProver prover, FiatShamirSignatureProver signatureProver) {
        this.prover = prover;
        this.signatureProver = signatureProver;
    }


    public T getSingleEntityByUri(String uri, User user, Class<T> clazz) {
        ResponseEntity<?> response = restClient.get()
                .uri(uri)
                .header("auth-user", user.getUsername())
                .header("auth-session", user.getAuthType().toString())
                .retrieve()
                .toEntity(clazz);

        return handleAuthentication(uri, user, clazz, response);
    }

    public T createEntity(String uri, T entity, User user, Class<T> clazz) {
        ResponseEntity<?> response = restClient.post()
                .uri(uri)
                .header("auth-user", user.getUsername())
                .header("auth-session", user.getAuthType().toString())
                .body(entity)
                .retrieve()
                .toEntity(clazz);

        return handleAuthentication(uri, user, clazz, response);
    }

    @SuppressWarnings("unchecked")
    private T handleAuthentication(String uri, User user, Class<T> clazz, ResponseEntity<?> response) {
        if (response.getHeaders().containsKey("auth-state")) {
            AuthenticationDTO authenticate = AuthUtils.createAuthenticationDtoFromHeaders(response.getHeaders());
            if (user.getAuthType().equals(AuthType.SIGNATURE)) {
                int rounds = Integer.parseInt(authenticate.payload());
                Signature signature = signatureProver.generateSignature(authenticate.sessionId(), user, rounds);
                authenticate = new AuthenticationDTO(authenticate.proverId(), authenticate.sessionId(), "", authenticate.sessionState());
                response = sendAuthentication(authenticate, signature, clazz);
                authenticate = AuthUtils.createAuthenticationDtoFromHeaders(response.getHeaders());
            } else if (user.getAuthType().equals(AuthType.FIATSHAMIR)) {
                while (authenticate.sessionState() != SessionState.VERIFIED
                        && authenticate.sessionState() != SessionState.FAILED) {
                    authenticate = prover.handleAuthentication(user, authenticate);
                    response = sendAuthentication(authenticate, clazz);
                    authenticate = AuthUtils.createAuthenticationDtoFromHeaders(response.getHeaders());
                }
            } else {
                authenticate = new AuthenticationDTO(authenticate.proverId(), authenticate.sessionId(), PasswordUtils.calcualtePasswordHash(user.getSecret()), authenticate.sessionState());
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

    private ResponseEntity<?> sendAuthentication(AuthenticationDTO authenticationDTO, Class<T> clazz) {
        return restClient.post().uri("/api/authenticate")
                .body(authenticationDTO)
                .retrieve()
                .toEntity(clazz);
    }

    private ResponseEntity<?> sendAuthentication(AuthenticationDTO authenticationDTO, Signature signature, Class<T> clazz) {
        SignatureAuthDTO signatureAuthDTO = new SignatureAuthDTO(authenticationDTO, signature);
        return restClient.post().uri("/api/signature")
                .body(signatureAuthDTO)
                .retrieve()
                .toEntity(clazz);
    }

}
