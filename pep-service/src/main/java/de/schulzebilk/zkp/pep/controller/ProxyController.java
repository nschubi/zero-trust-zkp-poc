package de.schulzebilk.zkp.pep.controller;

import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
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

    @PostConstruct
    public void init() {
        restClient = RestClient.create(resourceServiceUrl);
    }

    @GetMapping("/resource/**")
    public ResponseEntity<?> proxyGet(HttpServletRequest request) {
        String path = request.getRequestURI().substring("/api".length());
        LOG.info("Proxying GET request to resource service: {}", path);
        return restClient.get()
                .uri(resourceServiceUrl + path)
                .retrieve()
                .toEntity(Object.class);
    }

}
