package com.market.analysis.unit.infrastructure.config.security;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.market.analysis.infrastructure.config.security.LoginAttemptService;

/**
 * Unit tests for LoginAttemptService.
 * Covers the 3-failures / 30-minutes temporary block with a fixed clock.
 */
@DisplayName("LoginAttemptService Unit Tests")
class LoginAttemptServiceTest {

    private static final Instant NOW = Instant.parse("2026-09-30T10:00:00Z");

    private LoginAttemptService service;

    @BeforeEach
    void setUp() {
        service = new LoginAttemptService(Clock.fixed(NOW, ZoneOffset.UTC));
    }

    @Test
    @DisplayName("Fresh username should not be blocked and should have 3 attempts left")
    void freshUserShouldNotBeBlocked() {
        assertThat(service.isBlocked("admin")).isFalse();
        assertThat(service.remainingAttempts("admin")).isEqualTo(3);
    }

    @Test
    @DisplayName("One or two failures should not block but should reduce remaining attempts")
    void oneOrTwoFailuresShouldNotBlock() {
        assertThat(service.registerFailure("admin")).isFalse();
        assertThat(service.isBlocked("admin")).isFalse();
        assertThat(service.remainingAttempts("admin")).isEqualTo(2);

        assertThat(service.registerFailure("admin")).isFalse();
        assertThat(service.isBlocked("admin")).isFalse();
        assertThat(service.remainingAttempts("admin")).isEqualTo(1);
    }

    @Test
    @DisplayName("Third failure should trigger the block with zero attempts left")
    void thirdFailureShouldTriggerBlock() {
        service.registerFailure("admin");
        service.registerFailure("admin");

        assertThat(service.registerFailure("admin")).isTrue();
        assertThat(service.isBlocked("admin")).isTrue();
        assertThat(service.remainingAttempts("admin")).isZero();
    }

    @Test
    @DisplayName("Expired block should reset the failure counter on the same instance")
    void expiredBlockShouldResetCounter() {
        Clock start = Clock.fixed(NOW, ZoneOffset.UTC);
        MutableClock clock = new MutableClock(start);
        LoginAttemptService tracked = new LoginAttemptService(clock);
        tracked.registerFailure("admin");
        tracked.registerFailure("admin");
        tracked.registerFailure("admin");
        assertThat(tracked.isBlocked("admin")).isTrue();

        clock.advance(LoginAttemptService.LOCK_DURATION.plusSeconds(1));
        assertThat(tracked.isBlocked("admin")).isFalse();
        assertThat(tracked.remainingAttempts("admin")).isEqualTo(3);
    }

    @Test
    @DisplayName("Success should clear recorded failures")
    void successShouldClearFailures() {
        service.registerFailure("admin");
        service.registerFailure("admin");

        service.registerSuccess("admin");

        assertThat(service.isBlocked("admin")).isFalse();
        assertThat(service.remainingAttempts("admin")).isEqualTo(3);
    }

    @Test
    @DisplayName("Tracking should be case-insensitive and null-safe")
    void trackingShouldBeCaseInsensitiveAndNullSafe() {
        service.registerFailure("Admin");
        service.registerFailure("ADMIN");

        assertThat(service.remainingAttempts("admin")).isEqualTo(1);
        assertThat(service.isBlocked("ADMIN")).isFalse();
        assertThat(service.isBlocked(null)).isFalse();
        assertThat(service.remainingAttempts(null)).isEqualTo(3);
    }

    @Test
    @DisplayName("Failures for one username should not affect another")
    void failuresShouldBeIsolatedPerUsername() {
        service.registerFailure("admin");
        service.registerFailure("admin");
        service.registerFailure("admin");

        assertThat(service.isBlocked("admin")).isTrue();
        assertThat(service.isBlocked("other")).isFalse();
        assertThat(service.remainingAttempts("other")).isEqualTo(3);
    }

    /** Minimal mutable clock for time-travel assertions. */
    private static final class MutableClock extends Clock {

        private Instant current;
        private final ZoneOffset zone = ZoneOffset.UTC;

        MutableClock(Clock initial) {
            this.current = initial.instant();
        }

        void advance(java.time.Duration duration) {
            current = current.plus(duration);
        }

        @Override
        public ZoneOffset getZone() {
            return zone;
        }

        @Override
        public Clock withZone(java.time.ZoneId zone) {
            return this;
        }

        @Override
        public Instant instant() {
            return current;
        }
    }
}
