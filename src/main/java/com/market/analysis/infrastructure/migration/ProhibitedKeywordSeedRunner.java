package com.market.analysis.infrastructure.migration;

import java.util.List;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import com.market.analysis.domain.model.ProhibitedKeyword;
import com.market.analysis.domain.port.out.ProhibitedKeywordRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class ProhibitedKeywordSeedRunner implements CommandLineRunner {

    private static final List<String> DEFAULT_PROHIBITED_KEYWORDS = List.of(
            "ACQUISITION", "MERGER", "ETF", "FUND", "TRUST",
            "BULL", "BEAR", "2X", "3X",
            "THERAPEUTICS", "PHARMA", "BIO", "ONCOLOGY",
            "LP", "PARTNERS", "WARRANTS");

    private final ProhibitedKeywordRepository prohibitedKeywordRepository;

    @Override
    public void run(String... args) {
        if (!prohibitedKeywordRepository.findAll().isEmpty()) {
            log.info("ProhibitedKeywordSeedRunner: skipped seed because keywords already exist");
            return;
        }

        log.info("ProhibitedKeywordSeedRunner: seeding {} default keywords", DEFAULT_PROHIBITED_KEYWORDS.size());
        DEFAULT_PROHIBITED_KEYWORDS.stream()
                .map(ProhibitedKeyword::createActive)
                .forEach(prohibitedKeywordRepository::save);
        log.info("ProhibitedKeywordSeedRunner: seed completed");
    }
}
