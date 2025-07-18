package de.schulzebilk.zkp.pep.controller;

import de.schulzebilk.zkp.core.dto.AuthenticationDTO;
import de.schulzebilk.zkp.core.dto.UserDTO;
import de.schulzebilk.zkp.pep.client.PdpWebClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigInteger;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final PdpWebClient pdpWebClient;

    @Autowired
    public AuthController(PdpWebClient pdpWebClient) {
        this.pdpWebClient = pdpWebClient;
    }

    @GetMapping("/mod")
    public ResponseEntity<BigInteger> getPublicModulus() {
        BigInteger publicModulus = pdpWebClient.getPublicModulus();
        return ResponseEntity.ok(publicModulus);
    }

    @PostMapping("/register")
    public ResponseEntity<String> registerProver(@RequestBody UserDTO userDTO) {
        String response = pdpWebClient.registerProver(userDTO);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/authenticate")
    public ResponseEntity<AuthenticationDTO> authenticate(@RequestBody AuthenticationDTO authenticationDTO){
        AuthenticationDTO response = pdpWebClient.authenticate(authenticationDTO);
        return ResponseEntity.ok(response);
    }
}
