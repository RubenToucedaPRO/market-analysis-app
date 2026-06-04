package com.market.analysis.infrastructure.monitoring;

import java.time.Instant;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.market.analysis.domain.port.out.CandleHistoryRepository;
import com.market.analysis.infrastructure.persistence.repository.JpaApiCallRateRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
@RequiredArgsConstructor
public class CleanupTask {

    private final JpaApiCallRateRepository apiCallRepository;

    private final CandleHistoryRepository candleDataPort;

    private static final long CLEANUP_INTERVAL_MS = 86400; // 24 hours in seconds

    /**
     * Scheduled task that runs every 24 hours to clean up old API call logs from
     * the database.
     */
    @Scheduled(cron = "00 44 07 * * *")
    @Transactional
    public void executeCleanup() {
        Instant threshold = Instant.now().minusSeconds(CLEANUP_INTERVAL_MS);
        int deleted = apiCallRepository.deleteByOcurredAtBefore(threshold);
        log.info("Tarea de limpieza: Se han borrado {} registros de llamadas antiguos.", deleted);
    }

    /**
    * Scheduled task that runs every 24 hours to purge orphan candles from the
    * database.
    */
    @Scheduled(cron = "0 37 7 * * *")
    public void runPurge() {
        log.info("Iniciando tarea programada de purga...");
        candleDataPort.purgeOrphanCandles();
        log.info("Tarea de purga finalizada.");
    }
}
