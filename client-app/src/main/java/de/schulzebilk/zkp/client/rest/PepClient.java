package de.schulzebilk.zkp.client.rest;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.client.RestClient;

public abstract class PepClient {

    @Value("${service.url.pep}")
    protected String pepServiceUrl;

    protected RestClient restClient;



    @PostConstruct
    public void init() {
        restClient = RestClient.create(pepServiceUrl);
    }

}
