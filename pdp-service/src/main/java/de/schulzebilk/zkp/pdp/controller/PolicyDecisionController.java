package de.schulzebilk.zkp.pdp.controller;

import de.schulzebilk.zkp.core.dto.AuthenticationDTO;
import de.schulzebilk.zkp.core.dto.RegisterProverDTO;
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
    private final FiatShamirVerifierService fiatShamirVerifierService;

    @Autowired
    public PolicyDecisionController(PolicyAdministratorService policyAdministratorService, FiatShamirVerifierService fiatShamirVerifierService) {
        this.policyAdministratorService = policyAdministratorService;
        this.fiatShamirVerifierService = fiatShamirVerifierService;
    }

    @GetMapping("/mod")
    public ResponseEntity<BigInteger> getPublicModulus() {
        BigInteger publicModulus = fiatShamirVerifierService.getPublicMod();
        return ResponseEntity.ok(publicModulus);
    }

    @PostMapping("/register")
    public ResponseEntity<String> registerProver(@RequestBody RegisterProverDTO registerProverDTO) {
        fiatShamirVerifierService.registerProver(registerProverDTO.proverId(), registerProverDTO.proverKey());
        String response = "Prover with ID " + registerProverDTO.proverId() + " registered successfully.";
        return ResponseEntity.ok(response);
    }

    @PostMapping("/authenticate")
    public ResponseEntity<AuthenticationDTO> authenticate(@RequestBody AuthenticationDTO authenticationDTO){
        AuthenticationDTO response = policyAdministratorService.handleAuthentication(authenticationDTO);
        return ResponseEntity.ok(response);
    }

}
