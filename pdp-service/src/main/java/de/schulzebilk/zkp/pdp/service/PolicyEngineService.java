package de.schulzebilk.zkp.pdp.service;

import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;

import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@Service
public class PolicyEngineService {

    private final static Logger LOG = LoggerFactory.getLogger(PolicyEngineService.class);

    private final int GET_FACTOR = 1;
    private final int POST_FACTOR = 2;

    @Value("${pdp.policy.fiatShamir.fixedRounds}")
    private int FIXED_ROUNDS;

    private final Map<String, Integer> endpointMap = new HashMap<>();

    @PostConstruct
    public void init() {
        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(
                        Objects.requireNonNull(getClass().getClassLoader().getResourceAsStream("endpoints.csv"))))) {
            String line = br.readLine();
            while ((line = br.readLine()) != null) {
                String[] fields = line.split(";");
                endpointMap.put(fields[0], Integer.valueOf(fields[1]));
                LOG.info("Endpoint loaded: {} with rounds {}", fields[0], fields[1]);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Basic implementation of a trust algorithm that checks the prover ID and URI.
     * The Algorithm checks whether the prover ID aka the username is valid and whether the user is
     * allowed to access the URI. Furthermore, it checks whether the URI is valid.
     *
     * @param user The username or prover ID.
     * @param endpoint  The endpoint to be checked against the policy.
     * @return -1 if the request is forbidden or fails. Otherwise, returns a positive
     * integer with the number of rounds that the prover has to perform.
     */
    public int trustAlgorithm(String user, String endpoint, HttpMethod httpMethod) {
        LOG.debug("Trust algorithm called for user: {}, endpoint: {}, method: {}", user, endpoint, httpMethod);
        if (user == null || user.isEmpty() || endpoint == null || endpoint.isEmpty() || httpMethod == null) {
            LOG.error("Invalid input for trust algorithm. User: {}, Endpoint : {}, Method: {}", user, endpoint, httpMethod);
            return -1;
        }

        if (!endpointMap.containsKey(endpoint)) {
            LOG.error("Endpoint {} not found in policy engine.", endpoint);
            return -1;
        }

        if (FIXED_ROUNDS > 0) {
            LOG.info("Using fixed rounds: {}", FIXED_ROUNDS);
            return FIXED_ROUNDS;
        }
        return endpointMap.get(endpoint) * (httpMethod == HttpMethod.GET ? GET_FACTOR : POST_FACTOR);
    }
}
