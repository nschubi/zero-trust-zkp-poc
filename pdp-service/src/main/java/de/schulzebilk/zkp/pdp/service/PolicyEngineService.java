package de.schulzebilk.zkp.pdp.service;

import org.springframework.stereotype.Service;

@Service
public class PolicyEngineService {


    /**
     * Basic implementation of a trust algorithm that checks the prover ID and URI.
     * The Algorithm checks whether the prover ID aka the username is valid and whether the user is
     * allowed to access the URI. Furthermore, it checks whether the URI is valid.
     *
     * @param user The username or prover ID.
     * @param uri  The URI to be checked against the policy.
     * @return -1 if the request is forbidden or fails. Otherwise, returns a positive
     * integer with the number of rounds that the prover has to perform.
     */
    public int trustAlgorithm(String user, String uri) {
        if (user == null || user.isEmpty() || uri == null || uri.isEmpty()) {
            throw new IllegalArgumentException("Username and URI must not be null or empty. User: " + user + ", URI: " + uri);
        }

        //TODO check username against database
        //TODO check uri against database
        return 20;
    }
}
