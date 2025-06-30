package de.schulzebilk.zkp.client.controller;

import de.schulzebilk.zkp.client.PepWebClient;
import de.schulzebilk.zkp.core.model.Person;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/health")
public class HealthController {

    private final PepWebClient pepWebClient;

    @Autowired
    public HealthController(PepWebClient pepWebClient) {
        this.pepWebClient = pepWebClient;
    }

    @GetMapping
    public String healthCheck() {
        return "Client Application is running!";
    }

    @GetMapping("/person")
    public String personCheck() {
        Person person = pepWebClient.findPersonById(1L);
        return "Person found: " + (person != null ? person.firstName() + " " + person.lastName()
                : "No person found with ID 1");
    }
}
