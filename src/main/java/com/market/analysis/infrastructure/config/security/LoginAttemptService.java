package com.market.analysis.infrastructure.config.security;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Tracks failed login attempts per username and temporarily blocks the account
 * after too many failures. In-memory on purpose: the application is single-user
 * and the block is a short-lived brute-force mitigation, not durable state.
 *
 * <p>Thread-safe. Only usernames are stored — never passwords or secrets.
 */
public class LoginAttemptService {

    /** Failed attempts that trigger a temporary block. */
    public static final int MAX_ATTEMPTS = 3;

    /** How long a blocked username stays blocked. */
    public static final Duration LOCK_DURATION = Duration.ofMinutes(30);

    private final Clock clock;
    private final ConcurrentMap<String, Attempt> attempts = new ConcurrentHashMap<>();

    public LoginAttemptService(Clock clock) {
        this.clock = Objects.requireNonNull(clock, "Clock cannot be null");
    }

    /**
     * Registers a failed login. Returns {@code true} when this failure just
     * triggered the temporary block.
     */
    public boolean registerFailure(String username) {
        String key = normalize(username);
        Instant now = Instant.now(clock);
        AtomicBoolean justLocked = new AtomicBoolean(false);
        attempts.compute(key, (ignored, previous) -> {
            int failures = (previous == null ? 0 : previous.failures()) + 1;
            if (failures >= MAX_ATTEMPTS) {
                justLocked.set(true);
                return new Attempt(failures, now.plus(LOCK_DURATION));
            }
            return new Attempt(failures, null);
        });
        return justLocked.get();
    }

    /** Clears any recorded failures for the username (successful login). */
    public void registerSuccess(String username) {
        attempts.remove(normalize(username));
    }

    /** Returns {@code true} while the username is inside the block window. */
    public boolean isBlocked(String username) {
        String key = normalize(username);
        Attempt attempt = attempts.get(key);
        if (attempt == null || attempt.lockUntil() == null) {
            return false;
        }
        if (Instant.now(clock).isBefore(attempt.lockUntil())) {
            return true;
        }
        attempts.remove(key, attempt);
        return false;
    }

    /** Remaining attempts before the block triggers (0 when already blocked). */
    public int remainingAttempts(String username) {
        Attempt attempt = attempts.get(normalize(username));
        int failures = attempt == null ? 0 : attempt.failures();
        return Math.max(0, MAX_ATTEMPTS - failures);
    }

    /**
     * Test support: drops all tracked state so MockMvc tests start clean.
     */
    public void clear() {
        attempts.clear();
    }

    private static String normalize(String username) {
        return username == null ? "" : username.trim().toLowerCase(Locale.ROOT);
    }

    private record Attempt(int failures, Instant lockUntil) {
    }
}
