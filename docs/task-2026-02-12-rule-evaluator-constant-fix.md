# Task: Fix RuleEvaluator CONSTANT Indicator Support

**Date:** February 12, 2026  
**Branch:** copilot/add-strategy-selector-ticker  
**PR:** #18 - Integrate declarative strategy evaluation with ticker registration

## Summary

Fixed a critical bug in `RuleEvaluator` domain service where rules using `CONSTANT` as target code were failing because the indicator code was not recognized in the switch statement. The bug was discovered during persistence feature validation when test `RuleEvaluatorTest.shouldPassWhenVolumeGreaterThanConstant` failed with `AssertionFailedError: Expecting value to be true but was false`.

## Root Cause Analysis

The `RuleEvaluator.getIndicatorValue()` method handles technical indicator codes in a switch statement. When a rule specified `targetCode("CONSTANT")` with `targetParam(5000000.0)`, the method would return `null` because there was no case for "CONSTANT". This caused the evaluation to fail at the null-check before comparison, returning `false` incorrectly.

The method had a case for "VALUE" but not "CONSTANT", even though tests and production code were using "CONSTANT" as the standard indicator code for constant values.

## Code Changes

### File: [RuleEvaluator.java](../src/main/java/com/market/analysis/domain/service/RuleEvaluator.java)

**Line 70 - Changed:**
```java
// Before:
case "VALUE" -> param != null ? BigDecimal.valueOf(param) : null;

// After:
case "CONSTANT", "VALUE" -> param != null ? BigDecimal.valueOf(param) : null;
```

**Rationale:**
- Added support for "CONSTANT" indicator code (primary)
- Kept "VALUE" for backward compatibility
- Both codes return the parameter as a BigDecimal for numeric comparison

### File: [AnalyzeTickerControllerTest.java](../src/test/java/com/market/analysis/unit/presentation/controller/AnalyzeTickerControllerTest.java)

**Lines 128-129 and 148-149 - Fixed Mockito matcher misuse:**
```java
// Before:
verify(model, times(1)).addAttribute("tickers", any(List.class));
verify(model, times(1)).addAttribute("strategies", any(List.class));

// After:
verify(model, times(1)).addAttribute(eq("tickers"), any(List.class));
verify(model, times(1)).addAttribute(eq("strategies"), any(List.class));
```

**Rationale:**
- When using matchers in Mockito `verify()`, all arguments must be provided by matchers
- Cannot mix raw values ("tickers") with matchers (any(List.class))
- Added `eq()` matcher to make both arguments explicit matchers
- Added import: `import static org.mockito.ArgumentMatchers.eq;`

## Test Results

### Before Fix:
```
Tests run: 378, Failures: 1, Errors: 1, Skipped: 0

RuleEvaluatorTest.shouldPassWhenVolumeGreaterThanConstant [FAILED]
AnalyzeTickerControllerTest.testGetAllTickersEmpty [ERROR]
```

### After Fix:
```
Tests run: 378, Failures: 0, Errors: 0, Skipped: 0

BUILD SUCCESS ✅
```

### Test Coverage:
- `RuleEvaluator` Domain Service: 12/12 tests passing
  - Volume comparison with CONSTANT: ✅ `shouldPassWhenVolumeGreaterThanConstant`
  - Volume comparison with AVG_VOLUME: ✅ `shouldPassWhenVolumeGreaterThanAverage`
  - Other operator and indicator tests: ✅ All 10 additional tests
- `AnalyzeTickerController` Unit Tests: 7/7 tests passing
- Full test suite: 378/378 tests passing

## Impact Analysis

### Architecture Compliance:
- ✅ Maintains Clean Architecture - pure domain logic in RuleEvaluator
- ✅ No infrastructure dependencies
- ✅ Deterministic and testable

### Functionality Impact:
- **Fixed:** Rules with `targetCode("CONSTANT")` now evaluate correctly
- **Preserved:** Rules with other codes (PRICE, SMA, etc.) unaffected
- **Backward Compatible:** "VALUE" code still supported as alternative

### Performance:
- No performance impact - simple switch case addition

## Technical Decisions Documented

### Why "CONSTANT" vs "VALUE"?
The codebase convention uses "CONSTANT" as the primary indicator code for fixed numeric values in rules. Test data consistently used `targetCode("CONSTANT")`, making it the expected API. The "VALUE" case appears to be alternative naming that wasn't used in practice.

### Mockito Matcher Rules Applied:
- Rule: When using matchers in verify(), all arguments must be matchers
- Applied consistently across all verify() calls
- Prevents Mockito exceptions and ensures type safety

## Testing Strategy

1. **Unit Tests:** All domain tests validate indicator evaluation
2. **Integration Check:** Full test suite (378 tests) validates no regressions
3. **Compilation:** mvn clean compile successful (93 source files)

## Recommended Next Steps

1. **Code Review:** Verify fix aligns with architectural patterns
2. **Merge:** Incorporate into main branch when persistence feature PR reviews
3. **Documentation:** Update any API docs mentioning indicator codes to list both "CONSTANT" and "VALUE" options
4. **Future Enhancement:** Consider creating an enum for indicator codes to prevent similar missed cases

## Files Modified

```
src/main/java/com/market/analysis/domain/service/RuleEvaluator.java
src/test/java/com/market/analysis/unit/presentation/controller/AnalyzeTickerControllerTest.java
```

## Deployment Notes

- **Database Migration:** Not required
- **Configuration Changes:** None
- **Backward Compatibility:** Fully maintained
- **Testing:** All 378 unit tests passing

---

**Status:** ✅ COMPLETE - All tests passing, compilation clean, ready for merge.
