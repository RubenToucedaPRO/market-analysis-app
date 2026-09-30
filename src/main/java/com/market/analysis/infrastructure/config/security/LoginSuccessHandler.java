package com.market.analysis.infrastructure.config.security;

import java.io.IOException;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.security.core.Authentication;
import org.springframework.security.web.DefaultRedirectStrategy;
import org.springframework.security.web.RedirectStrategy;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Resets the failure counter after a successful login and sends the user to
 * the analysis dashboard. Logs username and IP only — never credentials.
 */
@Slf4j
@RequiredArgsConstructor
public class LoginSuccessHandler implements AuthenticationSuccessHandler {

    private static final String TARGET_URL = "/analysis";

    private final LoginAttemptService loginAttemptService;
    private final RedirectStrategy redirectStrategy = new DefaultRedirectStrategy();

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
            Authentication authentication) throws IOException, ServletException {
        loginAttemptService.registerSuccess(authentication.getName());
        log.info("login_success user={} ip={}", authentication.getName(), request.getRemoteAddr());
        redirectStrategy.sendRedirect(request, response, TARGET_URL);
    }
}
