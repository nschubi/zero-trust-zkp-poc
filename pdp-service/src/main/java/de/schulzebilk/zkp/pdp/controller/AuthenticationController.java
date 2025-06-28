package de.schulzebilk.zkp.pdp.controller;

import de.schulzebilk.zkp.core.model.auth.AuthenticationRequest;
import de.schulzebilk.zkp.pdp.service.AuthenticationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthenticationController {

    private final AuthenticationService authenticationService;

    @Autowired
    public AuthenticationController(AuthenticationService authenticationService) {
        this.authenticationService = authenticationService;
    }

    @PostMapping("/authenticate")
    public Boolean authenticate(@RequestBody AuthenticationRequest request) {
        return authenticationService.authenticate(
                request.username(),
                request.password()
        );
    }
}
