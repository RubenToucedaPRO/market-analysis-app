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
import com.market.analysis.infrastructure.config.security.LoginAttemptService;
import com.market.analysis.presentation.controller.HomeController;

/**
 * MockMvc tests for the TFM login hardening: 3 failures lock the username for
 * 30 minutes, even the correct password is rejected while blocked, and a
 * successful login resets the counter.
 */
@DisplayName("Login Lockout Tests")
@WebMvcTest(HomeController.class)
@Import(SecurityConfig.class)
class LoginLockoutTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private LoginAttemptService loginAttemptService;

    @MockitoBean
    private UserDetailsService userDetailsService;

    @BeforeEach
    void setUp() {
        loginAttemptService.clear();
        var user = User.builder()
                .username("admin")
                .password("{noop}admin")
                .roles("USER")
                .build();
        when(userDetailsService.loadUserByUsername("admin")).thenReturn(user);
    }

    @Test
    @DisplayName("First two failures should redirect to /login?error")
    void firstTwoFailuresShouldRedirectToError() throws Exception {
        mockMvc.perform(post("/login")
                        .with(csrf())
                        .param("username", "admin")
                        .param("password", "wrong"))
                .andExpect(status().is3xxRedirection())
                .andExpect(result -> assertThat(result.getResponse().getRedirectedUrl())
                        .isEqualTo("/login?error"));

        mockMvc.perform(post("/login")
                        .with(csrf())
                        .param("username", "admin")
                        .param("password", "wrong"))
                .andExpect(status().is3xxRedirection())
                .andExpect(result -> assertThat(result.getResponse().getRedirectedUrl())
                        .isEqualTo("/login?error"));
    }

    @Test
    @DisplayName("Third failure should redirect to /login?locked")
    void thirdFailureShouldRedirectToLocked() throws Exception {
        failLogin();
        failLogin();

        mockMvc.perform(post("/login")
                        .with(csrf())
                        .param("username", "admin")
                        .param("password", "wrong"))
                .andExpect(status().is3xxRedirection())
                .andExpect(result -> assertThat(result.getResponse().getRedirectedUrl())
                        .isEqualTo("/login?locked"));
    }

    @Test
    @DisplayName("Correct password should be rejected while blocked")
    void correctPasswordShouldBeRejectedWhileBlocked() throws Exception {
        failLogin();
        failLogin();
        failLogin();

        mockMvc.perform(post("/login")
                        .with(csrf())
                        .param("username", "admin")
                        .param("password", "admin"))
                .andExpect(status().is3xxRedirection())
                .andExpect(result -> assertThat(result.getResponse().getRedirectedUrl())
                        .isEqualTo("/login?locked"));
    }

    @Test
    @DisplayName("Successful login should reset the counter back to 3 attempts")
    void successShouldResetCounter() throws Exception {
        failLogin();

        mockMvc.perform(post("/login")
                        .with(csrf())
                        .param("username", "admin")
                        .param("password", "admin"))
                .andExpect(status().is3xxRedirection())
                .andExpect(result -> assertThat(result.getResponse().getRedirectedUrl())
                        .isEqualTo("/analysis"));

        assertThat(loginAttemptService.remainingAttempts("admin")).isEqualTo(3);
    }

    @Test
    @DisplayName("GET /login?locked should render the login page with the locked warning")
    void lockedPageShouldRenderWarning() throws Exception {
        mockMvc.perform(get("/login").queryParam("locked", ""))
                .andExpect(status().isOk());
    }

    private void failLogin() throws Exception {
        mockMvc.perform(post("/login")
                .with(csrf())
                .param("username", "admin")
                .param("password", "wrong"));
    }
}
