package de.schulzebilk.zkp.pep.service;

import de.schulzebilk.zkp.pep.client.PdpWebClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class PolicyService {

    private final PdpWebClient pdpWebClient;

    @Autowired
    public PolicyService(PdpWebClient pdpWebClient) {
        this.pdpWebClient = pdpWebClient;
    }

}
