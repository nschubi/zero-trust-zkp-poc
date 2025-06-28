package de.schulzebilk.zkp.pep.controller;

import de.schulzebilk.zkp.pep.service.PolicyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/health")
public class HealthController {

    private final PolicyService policyService;

    @Autowired
    public HealthController(PolicyService policyService) {
        this.policyService = policyService;
    }

    @GetMapping
    public String healthCheck() {
        return "Policy Enforcement Point is running!";
    }

    @GetMapping("/auth")
    public String authCheck() {
        boolean isAuthorized = policyService.isAuthorized("testUser", "testPassword");
        return isAuthorized ? "User is authorized!" : "User is not authorized!";
    }


}
