package com.market.analysis.unit.application.job;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Duration;
import java.time.Instant;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.market.analysis.application.job.BackgroundJob;
import com.market.analysis.application.job.JobStatus;

@DisplayName("BackgroundJob Unit Tests")
class BackgroundJobTest {

    @Test
    @DisplayName("Should transition through the full lifecycle")
    void testLifecycle() {
        BackgroundJob job = new BackgroundJob("job-1", "kind", 7L, Instant.now());

        assertEquals(JobStatus.PENDING, job.getStatus());
        assertTrue(job.isActive());
        assertTrue(!job.isTerminal());

        job.markRunning();
        assertEquals(JobStatus.RUNNING, job.getStatus());

        job.markDone();
        assertEquals(JobStatus.DONE, job.getStatus());
        assertTrue(job.isTerminal());
        assertTrue(!job.isActive());
    }

    @Test
    @DisplayName("Should reject invalid state transitions")
    void testInvalidTransitions() {
        BackgroundJob job = new BackgroundJob("job-1", "kind", 7L, Instant.now());
        job.markRunning();
        job.fail("boom");

        assertEquals(JobStatus.FAILED, job.getStatus());
        assertEquals("boom", job.getErrorDetail());
        assertThrows(IllegalStateException.class, job::markRunning);
        assertThrows(IllegalStateException.class, job::markDone);
        assertThrows(IllegalStateException.class, () -> job.fail("x"));
    }

    @Test
    @DisplayName("Should store flow-specific attributes")
    void testAttributes() {
        BackgroundJob job = new BackgroundJob("job-1", "kind", 7L, Instant.now());

        job.setAttribute("generated", "true");

        assertEquals("true", job.getAttribute("generated"));
        assertEquals(null, job.getAttribute("missing"));
    }

    @Test
    @DisplayName("Should expire only terminal jobs older than TTL")
    void testExpiryBoundary() {
        BackgroundJob job = new BackgroundJob("job-1", "kind", 7L, Instant.now().minusSeconds(3600));

        assertTrue(!job.isExpired(Instant.now(), Duration.ZERO));

        job.markRunning();

        assertTrue(!job.isExpired(Instant.now(), Duration.ZERO));

        job.markDone();

        assertTrue(job.isExpired(Instant.now(), Duration.ZERO));
        assertTrue(!job.isExpired(Instant.now(), Duration.ofHours(2)));
    }
}
