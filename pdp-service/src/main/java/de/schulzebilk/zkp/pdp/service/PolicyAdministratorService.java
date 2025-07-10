package de.schulzebilk.zkp.pdp.service;

import de.schulzebilk.zkp.core.auth.AuthenticationData;
import de.schulzebilk.zkp.core.auth.SessionState;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class PolicyAdministratorService {

    private final FiatShamirVerifierService fiatShamirVerifierService;

    @Autowired
    public PolicyAdministratorService(FiatShamirVerifierService fiatShamirVerifierService) {
        this.fiatShamirVerifierService = fiatShamirVerifierService;
    }

    public AuthenticationData handleAuthentication(AuthenticationData auth) {
        SessionState currentState = auth.sessionState();
        return null;
    }
    
}
