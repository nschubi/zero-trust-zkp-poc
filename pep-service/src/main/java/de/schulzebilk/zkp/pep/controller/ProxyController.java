package de.schulzebilk.zkp.pep.controller;

import de.schulzebilk.zkp.core.auth.SessionState;
import de.schulzebilk.zkp.core.dto.AuthenticationDTO;
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
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestClient;

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
    private final Map<String, String> sessionCache = new ConcurrentHashMap<>();

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
        String path = request.getRequestURI().substring("/api".length());
        String user = request.getHeader("auth-user");
        String payload = request.getHeader("auth-payload");
        LOG.info("User: {}, Payload: {}", user, payload);
        LOG.info("Proxying GET request to resource service: {}", path);

        AuthenticationDTO authenticationDTO = new AuthenticationDTO(
                user,
                null,
                payload,
                null
        );
        AuthenticationDTO authStatus = pdpWebClient.authenticate(authenticationDTO);
        if (!authStatus.sessionState().equals(SessionState.VERIFIED)) {
            LOG.warn("Authentication failed for user: {}", user);
            HttpHeaders headers = new HttpHeaders();
            headers.add("auth-user", authStatus.proverId());
            headers.add("auth-session", authStatus.sessionId());
            headers.add("auth-payload", authStatus.payload());
            headers.add("auth-state", authStatus.sessionState().name());
            sessionCache.put(authStatus.sessionId(), path);
            return ResponseEntity.status(HttpStatus.OK)
                    .headers(httpHeaders -> httpHeaders.addAll(headers))
                    .build();
        }

        return restClient.get()
                .uri(path)
                .retrieve()
                .toEntity(Object.class);
    }

    @PostMapping("/authenticate")
    public ResponseEntity<?> authenticate(@RequestBody AuthenticationDTO authenticationDTO) {
        AuthenticationDTO response = pdpWebClient.authenticate(authenticationDTO);
        if (response.sessionState() == SessionState.VERIFIED) {
            ResponseEntity<String> body = restClient.get()
                    .uri(sessionCache.get(authenticationDTO.sessionId()))
                    .retrieve()
                    .toEntity(String.class);
            return ResponseEntity.status(HttpStatus.OK)
                    .headers(headers -> {
                                headers.addAll(AuthUtils.createHeadersFromAuthenticationDto(response));
                                headers.setContentType(body.getHeaders().getContentType());
                            }
                    )
                    .body(body.getBody());
        }
        return ResponseEntity.status(HttpStatus.OK)
                .headers(httpHeaders -> httpHeaders.addAll(AuthUtils.createHeadersFromAuthenticationDto(response)))
                .build();
    }
}
