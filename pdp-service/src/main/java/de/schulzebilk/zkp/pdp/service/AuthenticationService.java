package de.schulzebilk.zkp.pdp.service;

import org.springframework.stereotype.Service;

@Service
public class AuthenticationService {

    public Boolean authenticate(String username, String password) {
        return username != null && !username.isEmpty() && password != null && !password.isEmpty();
    }
}
