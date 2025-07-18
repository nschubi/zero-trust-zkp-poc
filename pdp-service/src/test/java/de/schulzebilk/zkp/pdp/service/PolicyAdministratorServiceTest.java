package de.schulzebilk.zkp.pdp.service;

import de.schulzebilk.zkp.core.auth.SessionState;
import de.schulzebilk.zkp.core.dto.AuthenticationDTO;
import de.schulzebilk.zkp.core.dto.InitialAuthenticationDTO;
import de.schulzebilk.zkp.pdp.helper.ProverClient;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class PolicyAdministratorServiceTest {

    @Autowired
    private PolicyAdministratorService policyAdministratorService;

    @Autowired
    private FiatShamirVerifierService fiatShamirVerifierService;

    @Test
    void handleAuthentication() {
        // Setup
        ProverClient proverClient = new ProverClient("prover_test", fiatShamirVerifierService.getPublicMod(),
                "testPassword");
        fiatShamirVerifierService.registerProver(proverClient.getProverId(), proverClient.getProverKey());

        // Initial Message
        AuthenticationDTO auth = new AuthenticationDTO("prover_test", "FIATSHAMIR", null, null);
        InitialAuthenticationDTO initAuth = new InitialAuthenticationDTO(auth, "GET", "book");
        AuthenticationDTO response = policyAdministratorService.initiateAuthentication(initAuth);

        while(response.sessionState() != SessionState.VERIFIED && response.sessionState() != SessionState.FAILED) {
            System.out.println("Verifier: " + response);
            response = proverClient.handleAuthentication(response);
            System.out.println("Prover: " + response);
            response = policyAdministratorService.handleAuthentication(response);
        }
        System.out.println("Verifier: " + response);
    }
}