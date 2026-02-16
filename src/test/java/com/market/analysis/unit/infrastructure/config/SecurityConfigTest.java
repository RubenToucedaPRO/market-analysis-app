package com.market.analysis.unit.infrastructure.config;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Integration tests for SecurityConfig.
 * Verifies that Basic Auth is properly configured and health endpoint is public.
 */
@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("SecurityConfig Integration Tests")
class SecurityConfigTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("Should allow public access to /actuator/health without authentication")
    void testHealthEndpointIsPublic() throws Exception {
        mockMvc.perform(get("/actuator/health"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Should require authentication for root endpoint")
    void testRootEndpointRequiresAuth() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Should allow access with valid credentials")
    void testValidCredentials() throws Exception {
        mockMvc.perform(get("/")
                .with(httpBasic("test-user", "test-password")))
                .andExpect(status().isNotFound()); // 404 because no controller at /, but authenticated
    }

    @Test
    @DisplayName("Should deny access with invalid credentials")
    void testInvalidCredentials() throws Exception {
        mockMvc.perform(get("/")
                .with(httpBasic("wrong-user", "wrong-password")))
                .andExpect(status().isUnauthorized());
    }
}
