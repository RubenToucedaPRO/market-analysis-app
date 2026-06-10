# Task: Domain Services Constants Refactoring

**Date:** 2026-06-10  
**Step:** 5 of Constant Extraction Plan (Domain Services)  
**Status:** COMPLETED

## Summary

Refactored domain services to eliminate hardcoded strings, replacing `Objects.requireNonNull` with typed `DomainValidationException` using error codes, extracting local constants for metric keys and templates, and using `IndicatorCode` enum consistently. All 1038 tests pass.

## Files Modified

### `src/main/java/com/market/analysis/domain/service/RiskRewardCalculator.java`
- Replaced all `Objects.requireNonNull` calls with `DomainValidationException` using typed error codes
- Extracted field name constants: `FIELD_ENTRY_PRICE`, `FIELD_TARGET_PRICE`, `FIELD_STOP_PRICE`, `FIELD_CAPITAL_TO_RISK`
- Added `requireNonNull()` helper method for consistent null validation
- New error code: `validation.entry_price_null` added to messages.properties

### `src/main/java/com/market/analysis/domain/service/EvaluateStrategyService.java`
- Extracted metric key constants: `METRIC_TOTAL_RULES`, `METRIC_PASSED_RULES`, `METRIC_FAILED_RULES`
- Extracted summary template constants: `SUMMARY_TEMPLATE`, `RULES_PASSED_TEMPLATE`, `SUFFIX_FAILED_RULES`, `MSG_RISK_PLAN_FAILED`
- Already used `DomainValidationException` and `EvaluationStatus` enum — no further changes needed

### `src/main/java/com/market/analysis/domain/service/PromptBuilder.java`
- Replaced `Objects.requireNonNull` with `DomainValidationException("validation.stock_null")`
- Extracted prompt template as `PROMPT_TEMPLATE` constant
- Already had `NOT_AVAILABLE` constant — no further changes needed

### `src/main/java/com/market/analysis/domain/service/FinvizFilterMapperImpl.java`
- Extracted `NULL_RULE_LABEL` constant for the null rule placeholder
- Already used `IndicatorCode` enum and `OPERATOR_ALIASES` — no further changes needed

### `src/main/resources/messages.properties`
- Added `validation.entry_price_null=Entry price cannot be null`

### Tests Updated
- `RiskRewardCalculatorTest.java` — 5 tests updated: `NullPointerException` → `DomainValidationException` with error code assertions
- `PromptBuilderTest.java` — 1 test updated: `NullPointerException` → `DomainValidationException` with error code assertion

## Files Already Complete (No Changes Needed)
- `StrategyObjective.java` — Already uses `DomainValidationException` with error codes and `IndicatorCode.SMA`
- `Strategy.java` — Already uses `DomainValidationException` with error codes
- `PromptResponseValidator.java` — Already has `REQUIRED_SECTIONS` and `STRICT_RETRY_SUFFIX` as constants

## Test Results

- **Tests run:** 1038, Failures: 0, Errors: 0, Skipped: 0
- **BUILD SUCCESS**

## Remaining Work (from extraction plan)

- **Application layer refactoring** (sections 3.1-3.8):
  - `AnalyzeAndPersistStockService.java` — `EvaluationStatus` enum, timezone constant
  - `ManageAnalyzeStockService.java` — error codes, stage constants
  - `SuggestTickersService.java` — error codes, warning codes
  - `ManageStrategyService.java` — error codes
  - `ManageRuleDefinitionService.java` — error codes
  - `ManageProhibitedKeywordService.java` — error codes
  - `HealthCheckService.java` — `HealthStatusCode` enum, message keys
  - `HealthCheckMapper.java` — `HealthStatusCode` enum
