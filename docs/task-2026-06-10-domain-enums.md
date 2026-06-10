# Task: Step 2 - Domain Enums (IndicatorCode, HealthStatusCode, EvaluationStatus)

## Date
2026-06-10

## Summary
Implemented Step 2 of the constant extraction plan: created three domain enums (`IndicatorCode`, `HealthStatusCode`, `EvaluationStatus`) and refactored all domain and application layer files to use them instead of scattered string literals.

## Changes Made

### New Files Created

#### 1. `domain/model/IndicatorCode.java`
- Enum with 19 values: PRICE, SMA, EMA, RSI, MACD_LINE, MACD_SIGNAL, MACD_HIST, BB_UPPER, BB_LOWER, ATR, VOLUME, AVG_VOLUME, OPEN, HIGH, LOW, PREV_CLOSE, CONSTANT, VALUE, UNKNOWN
- Includes `fromCode(String)` method with O(1) lookup via static HashMap
- Each value has a `getCode()` method returning the string representation

#### 2. `domain/model/HealthStatusCode.java`
- Enum with 3 values: UP, DOWN, DEGRADED
- Includes `fromStatus(String)` method for reverse lookup
- Each value has a `getStatus()` method returning the string representation

#### 3. `domain/model/EvaluationStatus.java`
- Enum with 2 values: PASSED, FAILED
- Each value has a `getStatus()` method returning the string representation

### Files Refactored

#### Domain Layer
| File | Change |
|------|--------|
| `RuleCapabilityCatalog.java` | Replaced 18 string literals in `Map.entry()` with `IndicatorCode.*.getCode()` |
| `RuleEvaluator.java` | Used `EvaluationStatus.PASSED/FAILED.getStatus()` for justification status; used `IndicatorCode.UNKNOWN.getCode()` for null code handling |
| `RiskRewardCalculator.java` | Used `IndicatorCode.SMA.getCode()` in `resolveSmaValue()` lookups |
| `EvaluateStrategyService.java` | Removed `PASSED`/`FAILED` string constants, now uses `EvaluationStatus` enum |
| `StrategyObjective.java` | Used `IndicatorCode.SMA.getCode()` in `validateSmaPeriod()` lookup |
| `FinvizFilterMapperImpl.java` | Replaced all indicator code strings with `IndicatorCode.*.getCode()` in `THOUSAND_SCALED_SUBJECTS`, `STATIC_VALUE_TARGETS`, and `MAPPINGS` |

#### Application Layer
| File | Change |
|------|--------|
| `HealthCheckService.java` | Used `HealthStatusCode.DOWN.getStatus()` and `HealthStatusCode.UP.getStatus()` in `determineOverallStatus()` |
| `HealthCheckMapper.java` | Used `HealthStatusCode.UP.getStatus()` for HTTP status code comparison |
| `AnalyzeAndPersistStockService.java` | Used `EvaluationStatus.PASSED/FAILED.getStatus()` in log message |

#### Presentation Layer
| File | Change |
|------|--------|
| `HealthCheckController.java` | Used `HealthStatusCode.UP.getStatus()` for HTTP status comparison |

## Technical Decisions

1. **Enum pattern with `getCode()`**: Each enum stores its string representation and provides a `getCode()` method. This allows type-safe references while maintaining backward compatibility with string-based lookups.

2. **O(1) lookup for IndicatorCode**: The `fromCode()` method uses a pre-computed `HashMap` instead of iterating `values()`, which is important for high-frequency rule evaluation.

3. **No changes to HealthStatus model**: The `HealthStatus` domain model keeps its `String status` field for now. The enum is used for comparisons and return values, but the model field remains a String to avoid cascading changes across the entire health check pipeline.

4. **FinvizFilterMapperImpl**: All indicator code strings in `MAPPINGS`, `THOUSAND_SCALED_SUBJECTS`, and `STATIC_VALUE_TARGETS` now reference `IndicatorCode` enum constants, ensuring consistency with the catalog.

## Test Results
- **1038 tests, 0 failures, 0 errors, 0 skipped**
- All existing tests pass without modification (enums are backward-compatible)

## Architecture Compliance
- Enums are in `domain/model/` (pure domain layer)
- No Spring/framework dependencies in enum classes
- Follows Clean Architecture: domain defines enums, application/presentation consume them
- No `MessageSource` or i18n imports in domain layer

## SonarQube Considerations
- No new code smells introduced
- Enums use `final` fields (immutable)
- No magic numbers or strings remaining in refactored code
- `IndicatorCode.fromCode()` throws `IllegalArgumentException` for unknown codes (fail-fast)

## Next Steps
- **Step 3**: Expand `WebConstants.java` with presentation layer constants (model attributes, template names, redirect URLs)
- Consider migrating `HealthStatus.status` field from `String` to `HealthStatusCode` in a future step
