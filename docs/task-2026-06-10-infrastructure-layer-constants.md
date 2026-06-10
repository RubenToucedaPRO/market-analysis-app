# Task: Infrastructure Layer Constants Extraction

**Date:** 2026-06-10  
**Step:** 4 of Constant Extraction Plan  
**Status:** COMPLETED

## Summary

Extracted all hardcoded strings from Infrastructure layer into centralized constants. Created `ApiConstants.java` (25+ constants for external API endpoints, CSS selectors, JSON field keys). Refactored 7 infrastructure classes. All 1038 tests pass.

## Files Created

### `src/main/java/com/market/analysis/infrastructure/config/ApiConstants.java`
Central repository for external API configuration constants:
- **Finnhub endpoints:** `FINNHUB_ENDPOINT_QUOTE`, `FINNHUB_ENDPOINT_PROFILE`
- **Polygon URI/params:** `POLYGON_URI_AGGREGATES`, `POLYGON_PARAM_MULTIPLIER`, `POLYGON_PARAM_ADJUSTED`, `POLYGON_PARAM_SORT`, `POLYGON_PARAM_LIMIT`
- **Polygon JSON fields:** `JSON_CLOSE`, `JSON_VOLUME`, `JSON_HIGH`, `JSON_LOW`, `JSON_OPEN`, `JSON_TIMESTAMP`, `JSON_RESULTS`, `JSON_STATUS`, `JSON_NEXT_URL`
- **Finviz selectors:** `FINVIZ_SELECTOR_LINK`, `FINVIZ_SELECTOR_TABLE`, `FINVIZ_SELECTOR_TBODY`, `FINVIZ_SELECTOR_TD`, `FINVIZ_SELECTOR_A`
- **Finviz query params:** `FINVIZ_QUERY_CHANGE`, `FINVIZ_QUERY_MARKET_CAP`, `FINVIZ_QUERY_PE`, `FINVIZ_QUERY_DIVIDEND_YIELD`
- **OpenRouter config:** `OPENROUTER_BASE_URL`, `OPENROUTER_HEADER_REFERER`, `OPENROUTER_DEFAULT_REFERER`
- **Finviz base URL:** `FINVIZ_BASE_URL`

## Files Modified

### `src/main/java/com/market/analysis/infrastructure/external/finnhub/FinnhubAdapter.java`
- `"/stock/quote"` → `ApiConstants.FINNHUB_ENDPOINT_QUOTE`
- `"/stock/profile2"` → `ApiConstants.FINNHUB_ENDPOINT_PROFILE`

### `src/main/java/com/market/analysis/infrastructure/external/polygon/PolygonAdapter.java`
- Hardcoded URI `/v2/aggs/` → `ApiConstants.POLYGON_URI_AGGREGATES`
- Hardcoded query params (`"multiplier"`, `"adjusted"`, etc.) → ApiConstants constants
- Hardcoded JSON keys (`"close"`, `"volume"`, etc.) → ApiConstants constants

### `src/main/java/com/market/analysis/infrastructure/external/finviz/JsoupFinvizAdapter.java`
- Hardcoded CSS selectors (`"a.screener-link"`, `"table.screener_table"`, etc.) → ApiConstants constants
- Hardcoded query params (`"f=geo_usa,ind_stocksonly,ta_sma20_pa"`) → ApiConstants constants

### `src/main/java/com/market/analysis/infrastructure/config/BeanConfig.java`
- `"https://openrouter.ai"` → `ApiConstants.OPENROUTER_BASE_URL`
- `"HTTP-Referer"` → `ApiConstants.OPENROUTER_HEADER_REFERER`
- `"https://market-analysis-app.local"` → `ApiConstants.OPENROUTER_DEFAULT_REFERER`

### `src/main/java/com/market/analysis/infrastructure/config/SlowQueryInspector.java`
- Extracted regex pattern, obfuscation replacement, and truncation suffix into named constants

### `src/main/java/com/market/analysis/infrastructure/config/ApiKeyObfuscatorInterceptor.java`
- Extracted regex pattern, obfuscation replacement, and log format into named constants

### `src/main/java/com/market/analysis/infrastructure/monitoring/HealthCheckAdapter.java`
- Extracted timeout value into `CONNECTION_VALIDATION_TIMEOUT_SECONDS` constant

### `src/main/java/com/market/analysis/infrastructure/persistence/repository/SqlStrategyEvaluationRepository.java`
- Changed Spanish error message "Stock no encontrado" → English "Stock not found"

### `src/test/java/.../SqlStrategyEvaluationRepositoryTest.java`
- Updated test to expect new English error message

## Test Results

- **Tests run:** 1038, Failures: 0, Errors: 0, Skipped: 0
- **BUILD SUCCESS**

## Remaining Work (from extraction plan)

- **P1:** Move DTOs from `application/dto/` to domain-native models (6 use-case interfaces import `application.dto.*`)
- **P2:** Create `RuleCapability.java` domain model to replace `RuleCapabilityDTO`
- **Domain services refactoring** (sections 2.1-2.9 of extraction plan)
- **Application layer refactoring** (sections 3.1-3.8 of extraction plan)
