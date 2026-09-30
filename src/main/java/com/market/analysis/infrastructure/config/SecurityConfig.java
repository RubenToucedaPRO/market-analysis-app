package com.market.analysis.infrastructure.config;

import java.time.Clock;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.market.analysis.infrastructure.config.security.LoginAttemptService;
import com.market.analysis.infrastructure.config.security.LoginBlockFilter;
import com.market.analysis.infrastructure.config.security.LoginFailureHandler;
import com.market.analysis.infrastructure.config.security.LoginSuccessHandler;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http, LoginAttemptService loginAttemptService)
            throws Exception {
        http
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(
                    "/",
                    "/login",
                    // Público para los health checks (Docker/Railway): no expone
                    // secretos, solo estado + tiempo de respuesta de la BD.
                    "/health",
                    "/css/**",
                    "/js/**",
                    "/images/**",
                    "/favicon.ico",
                    "/favicon.png"
                ).permitAll()
                .anyRequest().authenticated()
            )
            .formLogin(form -> form
                .loginPage("/login")
                .successHandler(new LoginSuccessHandler(loginAttemptService))
                .failureHandler(new LoginFailureHandler(loginAttemptService))
                .permitAll()
            )
            .logout(logout -> logout
                .logoutSuccessUrl("/login?logout")
                .permitAll()
            )
            .addFilterBefore(new LoginBlockFilter(loginAttemptService),
                    UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    @Bean
    public LoginAttemptService loginAttemptService() {
        return new LoginAttemptService(Clock.systemUTC());
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }

    @Bean
    public UserDetailsService userDetailsService(
            @Value("${app.security.username}") String username,
            @Value("${app.security.password}") String password) {
        var user = User.builder()
                .username(username)
                .password("{noop}" + password)
                .roles("USER")
                .build();
        return new InMemoryUserDetailsManager(user);
    }
}
