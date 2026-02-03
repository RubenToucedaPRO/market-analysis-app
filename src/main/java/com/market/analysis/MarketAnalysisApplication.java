package com.market.analysis;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

/**
 * Market Analysis Application Entry Point.
 * Spring Boot main application class.
 */
@SpringBootApplication
@EnableCaching
public class MarketAnalysisApplication {

    public static void main(String[] args) {
        SpringApplication.run(MarketAnalysisApplication.class, args);
    }
}
