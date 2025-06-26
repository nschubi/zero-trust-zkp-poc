package de.schulzebilk.zkp.pep.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.jupiter.api.Assertions.fail;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(HealthController.class)
class HealthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void healthCheck() {
        try {
            mockMvc.perform(get("/health"))
                    .andExpect(status().isOk())
                    .andExpect(content().string("Policy Enforcement Point is running!"));
        } catch (Exception e) {
            fail("Health check failed: " + e.getMessage());
        }
    }
}