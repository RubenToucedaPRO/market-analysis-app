package com.market.analysis.infrastructure.config.security;

import java.io.IOException;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.http.HttpMethod;
import org.springframework.security.web.DefaultRedirectStrategy;
import org.springframework.security.web.RedirectStrategy;
import org.springframework.web.filter.OncePerRequestFilter;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Rejects login submissions for temporarily blocked usernames before the
 * authentication providers run, even when the password is correct.
 * Logs username and IP only — never credentials.
 */
@Slf4j
@RequiredArgsConstructor
public class LoginBlockFilter extends OncePerRequestFilter {

    private static final String LOGIN_PATH = "/login";
    private static final String LOCKED_URL = "/login?locked";

    private final LoginAttemptService loginAttemptService;
    private final RedirectStrategy redirectStrategy = new DefaultRedirectStrategy();

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {
        if (isLoginSubmission(request) && loginAttemptService.isBlocked(request.getParameter("username"))) {
            log.warn("login_blocked user={} ip={}", request.getParameter("username"), request.getRemoteAddr());
            redirectStrategy.sendRedirect(request, response, LOCKED_URL);
            return;
        }
        filterChain.doFilter(request, response);
    }

    private static boolean isLoginSubmission(HttpServletRequest request) {
        if (!HttpMethod.POST.matches(request.getMethod())) {
            return false;
        }
        // requestURI minus contextPath works both in a real container (where
        // servletPath is "/login") and in MockMvc (where servletPath is "").
        String path = request.getRequestURI().substring(request.getContextPath().length());
        return LOGIN_PATH.equals(path);
    }
}
