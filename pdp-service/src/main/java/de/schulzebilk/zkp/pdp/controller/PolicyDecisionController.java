package de.schulzebilk.zkp.pdp.controller;

import de.schulzebilk.zkp.core.dto.AuthenticationDTO;
import de.schulzebilk.zkp.core.dto.InitialAuthenticationDTO;
import de.schulzebilk.zkp.core.dto.UserDTO;
import de.schulzebilk.zkp.pdp.service.FiatShamirVerifierService;
import de.schulzebilk.zkp.pdp.service.PolicyAdministratorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigInteger;

@RestController
public class PolicyDecisionController {

    private final PolicyAdministratorService policyAdministratorService;

    @Autowired
    public PolicyDecisionController(PolicyAdministratorService policyAdministratorService, FiatShamirVerifierService fiatShamirVerifierService) {
        this.policyAdministratorService = policyAdministratorService;
    }

    @GetMapping("/mod")
    public ResponseEntity<BigInteger> getPublicModulus() {
        BigInteger publicModulus = policyAdministratorService.getPublicMod();
        return ResponseEntity.ok(publicModulus);
    }

    @PostMapping("/register")
    public ResponseEntity<String> registerUser(@RequestBody UserDTO userDTO) {
        policyAdministratorService.registerUser(userDTO.userId(), userDTO.secret(), userDTO.authType());
        String response = "User with ID " + userDTO.userId() + " registered successfully.";
        return ResponseEntity.ok(response);
    }

    @PostMapping("/initiate")
    public ResponseEntity<AuthenticationDTO> initiate(@RequestBody InitialAuthenticationDTO initialAuthenticationDTO){
        AuthenticationDTO response = policyAdministratorService.initiateAuthentication(initialAuthenticationDTO);
        return ResponseEntity.ok(response);
    }


    @PostMapping("/authenticate")
    public ResponseEntity<AuthenticationDTO> authenticate(@RequestBody AuthenticationDTO authenticationDTO){
        AuthenticationDTO response = policyAdministratorService.handleAuthentication(authenticationDTO);
        return ResponseEntity.ok(response);
    }

}
