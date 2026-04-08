package com.market.analysis.infrastructure.migration;

import java.util.List;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import com.market.analysis.domain.model.RuleCapabilityCatalog;
import com.market.analysis.domain.model.RuleCapability;
import com.market.analysis.domain.model.RuleDefinition;
import com.market.analysis.domain.port.out.RuleDefinitionRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Startup component that sanitises persisted rule definitions against the
 * canonical {@link RuleCapabilityCatalog}.
 *
 * <p>On every application start it:</p>
 * <ol>
 *   <li>Logs a warning for each {@code RuleDefinition} whose {@code code} is no
 *       longer supported by the evaluator.</li>
 *   <li>Deletes those stale definitions from the database so that the UI never
 *       offers combinations that the engine cannot resolve (P2 – data
 *       migration).</li>
 *   <li>Corrects the {@code requiresParam} flag for definitions whose code is
 *       still valid but whose flag conflicts with the catalog.</li>
 * </ol>
 *
 * <p>This runner is intentionally idempotent: running it multiple times on a
 * clean dataset produces no changes.</p>
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class RuleDefinitionSanitizationRunner implements CommandLineRunner {

    private final RuleDefinitionRepository ruleDefinitionRepository;

    @Override
    public void run(String... args) {
        log.info("RuleDefinitionSanitizationRunner: starting catalog consistency check");

        List<RuleDefinition> all = ruleDefinitionRepository.findAll();
        int removed = 0;
        int corrected = 0;

        for (RuleDefinition rd : all) {
            if (!RuleCapabilityCatalog.isSupported(rd.getCode())) {
                log.warn("Removing stale rule definition id={} code='{}': not supported by the evaluator",
                        rd.getId(), rd.getCode());
                ruleDefinitionRepository.deleteById(rd.getId());
                removed++;
                continue;
            }

            boolean expectedRequiresParam = RuleCapabilityCatalog.getCapability(rd.getCode())
                    .map(RuleCapability::isRequiresParam)
                    .orElse(false);

            if (rd.isRequiresParam() != expectedRequiresParam) {
                log.warn(
                        "Correcting rule definition id={} code='{}': requiresParam={} -> {}",
                        rd.getId(), rd.getCode(), rd.isRequiresParam(), expectedRequiresParam);
                RuleDefinition correctedRd = RuleDefinition.builder()
                        .id(rd.getId())
                        .code(rd.getCode())
                        .name(rd.getName())
                        .requiresParam(expectedRequiresParam)
                        .description(rd.getDescription())
                        .build();
                ruleDefinitionRepository.save(correctedRd);
                corrected++;
            }
        }

        log.info("RuleDefinitionSanitizationRunner: finished – removed={} corrected={}", removed, corrected);
    }
}
