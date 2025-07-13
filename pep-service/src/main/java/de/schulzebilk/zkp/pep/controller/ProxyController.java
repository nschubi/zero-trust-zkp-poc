package de.schulzebilk.zkp.pep.controller;

import de.schulzebilk.zkp.core.auth.SessionState;
import de.schulzebilk.zkp.core.dto.AuthenticationDTO;
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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestClient;

@RestController
@RequestMapping("/api")
public class ProxyController {

    @Value("${service.url.resource}")
    private String resourceServiceUrl;

    private final static Logger LOG = LoggerFactory.getLogger(ProxyController.class);

    private RestClient restClient;
    private final PdpWebClient pdpWebClient;

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
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .headers(httpHeaders -> httpHeaders.addAll(headers))
                    .build();
        }

        return restClient.get()
                .uri(resourceServiceUrl + path)
                .retrieve()
                .toEntity(Object.class);
    }

}
