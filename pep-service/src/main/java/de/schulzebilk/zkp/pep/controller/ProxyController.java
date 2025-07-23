package de.schulzebilk.zkp.pep.controller;

import de.schulzebilk.zkp.core.auth.SessionState;
import de.schulzebilk.zkp.core.dto.AuthenticationDTO;
import de.schulzebilk.zkp.core.dto.InitialAuthenticationDTO;
import de.schulzebilk.zkp.core.dto.SignatureAuthDTO;
import de.schulzebilk.zkp.core.model.Signature;
import de.schulzebilk.zkp.core.util.AuthUtils;
import de.schulzebilk.zkp.pep.client.PdpWebClient;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestClient;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@RestController
@RequestMapping("/api")
public class ProxyController {

    @Value("${service.url.resource}")
    private String resourceServiceUrl;

    private final static Logger LOG = LoggerFactory.getLogger(ProxyController.class);

    private RestClient restClient;
    private final PdpWebClient pdpWebClient;
    private final Map<String, CachedRequest> sessionCache = new ConcurrentHashMap<>();

    @Autowired
    public ProxyController(PdpWebClient pdpWebClient) {
        this.pdpWebClient = pdpWebClient;
    }

    @PostConstruct
    public void init() {
        restClient = RestClient.create(resourceServiceUrl);
    }

    @GetMapping("/resource/**")
    public ResponseEntity<?> proxyGet(HttpServletRequest request) {
        return handleRequest(request, null, null);
    }

    @PostMapping("/resource/**")
    public ResponseEntity<?> proxyPost(HttpServletRequest request, @RequestBody String body) {
        return handleRequest(request, body, request.getContentType());
    }

    @PostMapping("/authenticate")
    public ResponseEntity<?> authenticate(@RequestBody AuthenticationDTO authenticationDTO) {
        AuthenticationDTO response = pdpWebClient.authenticate(authenticationDTO);
        return handlePdpAuthenticationResponse(response);
    }

    @PostMapping("/signature")
    public ResponseEntity<?> authenticateSignature(@RequestBody SignatureAuthDTO signatureAuthDTO) {
        AuthenticationDTO response = pdpWebClient.authenticateSignature(signatureAuthDTO);
        return handlePdpAuthenticationResponse(response);
    }

    private ResponseEntity<?> handlePdpAuthenticationResponse(AuthenticationDTO response) {
        if (response.sessionState() == SessionState.VERIFIED) {
            CachedRequest cachedRequest = sessionCache.get(response.sessionId());

            if (cachedRequest.method().equals("GET")) {
                ResponseEntity<String> body = restClient.get()
                        .uri(cachedRequest.uri())
                        .retrieve()
                        .toEntity(String.class);

                return ResponseEntity.status(HttpStatus.OK)
                        .headers(headers -> {
                            headers.addAll(AuthUtils.createHeadersFromAuthenticationDto(response));
                            headers.setContentType(body.getHeaders().getContentType());
                        })
                        .body(body.getBody());

            } else if (cachedRequest.method().equals("POST")) {
                ResponseEntity<String> body = restClient.post()
                        .uri(cachedRequest.uri())
                        .contentType(MediaType.valueOf(cachedRequest.contentType()))
                        .body(cachedRequest.body())
                        .retrieve()
                        .toEntity(String.class);

                return ResponseEntity.status(HttpStatus.OK)
                        .headers(headers -> {
                            headers.addAll(AuthUtils.createHeadersFromAuthenticationDto(response));
                            headers.setContentType(body.getHeaders().getContentType());
                        })
                        .body(body.getBody());
            }
        }
        return ResponseEntity.status(HttpStatus.OK)
                .headers(httpHeaders -> httpHeaders.addAll(AuthUtils.createHeadersFromAuthenticationDto(response)))
                .build();
    }

    private ResponseEntity<?> handleRequest(HttpServletRequest request, String body, String contentType) {
        String requestUri = request.getRequestURI().substring("/api".length());
        Path path = Paths.get(requestUri);
        String endpoint = path.getName(1).toString();

        String user = request.getHeader("auth-user");
        String session = request.getHeader("auth-session");
        String payload = request.getHeader("auth-payload");
        String state = request.getHeader("auth-state");

        LOG.info("User: {}, Payload: {}", user, payload);
        LOG.info("Proxying {} request to resource service: {}", request.getMethod(), endpoint);

        AuthenticationDTO authenticationDTO = new AuthenticationDTO(
                user, session, payload,
                state == null ? null : SessionState.valueOf(state)
        );
        InitialAuthenticationDTO initialAuth = new InitialAuthenticationDTO(
                authenticationDTO, request.getMethod(), endpoint);

        AuthenticationDTO authStatus = pdpWebClient.initiateAuthentication(initialAuth);

        if (!authStatus.sessionState().equals(SessionState.VERIFIED)) {
            return handleUnauthenticatedRequest(authStatus, requestUri, body, contentType, request.getMethod());
        }

        return executeRequest(requestUri, body, contentType, request.getMethod());
    }

    private ResponseEntity<?> handleUnauthenticatedRequest(AuthenticationDTO authStatus,
                                                           String requestUri, String body,
                                                           String contentType, String method) {
        LOG.warn("Authentication failed for user: {}", authStatus.proverId());

        HttpHeaders headers = new HttpHeaders();
        headers.add("auth-user", authStatus.proverId());
        headers.add("auth-session", authStatus.sessionId());
        headers.add("auth-payload", authStatus.payload());
        headers.add("auth-state", authStatus.sessionState().name());

        sessionCache.put(authStatus.sessionId(), new CachedRequest(
                requestUri, body, contentType, method));

        return ResponseEntity.status(HttpStatus.OK)
                .headers(httpHeaders -> httpHeaders.addAll(headers))
                .build();
    }

    private ResponseEntity<?> executeRequest(String requestUri, String body, String contentType, String method) {
        if ("GET".equals(method)) {
            return restClient.get()
                    .uri(requestUri)
                    .retrieve()
                    .toEntity(Object.class);
        } else if ("POST".equals(method)) {
            return restClient.post()
                    .uri(requestUri)
                    .contentType(MediaType.valueOf(contentType))
                    .body(body)
                    .retrieve()
                    .toEntity(Object.class);
        }
        throw new IllegalArgumentException("Unsupported HTTP method: " + method);
    }

    private record CachedRequest(
            String uri,
            String body,
            String contentType,
            String method
    ) {
    }

}
