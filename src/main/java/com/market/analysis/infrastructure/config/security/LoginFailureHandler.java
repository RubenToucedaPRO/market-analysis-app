package com.market.analysis.infrastructure.config.security;

import java.io.IOException;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;

import lombok.extern.slf4j.Slf4j;

/**
 * Counts failed logins and redirects blocked usernames to {@code /login?locked}.
 * Logs username, IP and remaining attempts only — never credentials.
 */
@Slf4j
public class LoginFailureHandler extends SimpleUrlAuthenticationFailureHandler {

    private static final String ERROR_URL = "/login?error";
    private static final String LOCKED_URL = "/login?locked";

    private final LoginAttemptService loginAttemptService;

    public LoginFailureHandler(LoginAttemptService loginAttemptService) {
        super(ERROR_URL);
        this.loginAttemptService = loginAttemptService;
    }

    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
            AuthenticationException exception) throws IOException, ServletException {
        String username = request.getParameter("username");
        String ip = request.getRemoteAddr();
        if (loginAttemptService.isBlocked(username)) {
            log.warn("login_blocked user={} ip={}", username, ip);
            getRedirectStrategy().sendRedirect(request, response, LOCKED_URL);
            return;
        }
        boolean justLocked = loginAttemptService.registerFailure(username);
        if (justLocked) {
            log.warn("login_blocked user={} ip={}", username, ip);
            getRedirectStrategy().sendRedirect(request, response, LOCKED_URL);
            return;
        }
        log.warn("login_failed user={} ip={} attemptsLeft={}", username, ip,
                loginAttemptService.remainingAttempts(username));
        super.onAuthenticationFailure(request, response, exception);
    }
}
