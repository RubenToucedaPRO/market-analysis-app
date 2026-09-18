package com.market.analysis.unit.infrastructure.external.finnhub;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import com.market.analysis.infrastructure.external.finnhub.FinnhubThrottler;

class FinnhubThrottlerTest {

        @Test
        void shouldAllowCallsWithinBudgetWithoutWaiting() {
                FinnhubThrottler throttler = new FinnhubThrottler(3, 60_000);

                long start = System.currentTimeMillis();
                throttler.acquire();
                throttler.acquire();
                throttler.acquire();
                long elapsed = System.currentTimeMillis() - start;

                assertTrue(elapsed < 1_000, "3 calls within budget of 3 should not wait, took " + elapsed + "ms");
        }

        @Test
        void shouldWaitUntilOldestCallExpiresWhenBudgetExceeded() {
                FinnhubThrottler throttler = new FinnhubThrottler(2, 400);

                throttler.acquire();
                throttler.acquire();

                long start = System.currentTimeMillis();
                throttler.acquire();
                long elapsed = System.currentTimeMillis() - start;

                assertTrue(elapsed >= 300, "3rd call over budget of 2 should wait ~400ms, waited " + elapsed + "ms");
                assertTrue(elapsed < 5_000, "wait should be bounded, waited " + elapsed + "ms");
        }

        @Test
        void shouldAllowImmediateCallAfterWindowSlides() throws InterruptedException {
                FinnhubThrottler throttler = new FinnhubThrottler(1, 200);

                throttler.acquire();
                Thread.sleep(400);

                long start = System.currentTimeMillis();
                throttler.acquire();
                long elapsed = System.currentTimeMillis() - start;

                assertTrue(elapsed < 1_000, "call after expired window should not wait, took " + elapsed + "ms");
        }
}
