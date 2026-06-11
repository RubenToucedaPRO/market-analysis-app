package com.market.analysis.unit.presentation.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.market.analysis.infrastructure.config.SecurityConfig;
import com.market.analysis.presentation.controller.HomeController;

@DisplayName("Security Integration Tests")
@WebMvcTest(HomeController.class)
@Import(SecurityConfig.class)
class SecurityConfigTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserDetailsService userDetailsService;

    @BeforeEach
    void setUp() {
        var user = User.builder()
                .username("admin")
                .password("{noop}admin")
                .roles("USER")
                .build();
        when(userDetailsService.loadUserByUsername("admin")).thenReturn(user);
    }

    @Test
    @DisplayName("GET / should be accessible without authentication")
    void homePageShouldBePublic() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /login should be accessible without authentication")
    void loginPageShouldBePublic() throws Exception {
        mockMvc.perform(get("/login"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /analysis should redirect to login when not authenticated")
    void protectedPageShouldRedirectToLogin() throws Exception {
        mockMvc.perform(get("/analysis"))
                .andExpect(status().is3xxRedirection())
                .andExpect(result -> {
                    String url = result.getResponse().getRedirectedUrl();
                    assertThat(url).contains("/login");
                });
    }

    @Test
    @DisplayName("POST /login with valid credentials should redirect to /analysis")
    void loginWithValidCredentialsShouldSucceed() throws Exception {
        mockMvc.perform(post("/login")
                        .with(csrf())
                        .param("username", "admin")
                        .param("password", "admin"))
                .andExpect(status().is3xxRedirection())
                .andExpect(result -> {
                    String url = result.getResponse().getRedirectedUrl();
                    assertThat(url).isEqualTo("/analysis");
                });
    }

    @Test
    @DisplayName("POST /login with invalid credentials should redirect back to login with error")
    void loginWithInvalidCredentialsShouldFail() throws Exception {
        mockMvc.perform(post("/login")
                        .with(csrf())
                        .param("username", "admin")
                        .param("password", "wrongpassword"))
                .andExpect(status().is3xxRedirection())
                .andExpect(result -> {
                    String url = result.getResponse().getRedirectedUrl();
                    assertThat(url).contains("/login");
                    assertThat(url).contains("error");
                });
    }
}
