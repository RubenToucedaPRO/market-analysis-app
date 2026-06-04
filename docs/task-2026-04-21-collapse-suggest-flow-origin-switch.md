# Collapse suggestion flow into AnalyzeAndPersistStockService

## Summary
This task simplified the suggestion and analysis flow so the suggestion use case now reuses the shared deterministic analysis/persistence service directly. The deterministic evaluator abstraction and the old add-suggested-to-analysis use case were removed, and the strategy UI now changes `origin` instead of copying via a separate service.

## What changed
- `SuggestTickersService` now calls `AnalyzeAndPersistStockService` for each candidate ticker using `StockOrigin.SUGGESTION_SNAPSHOT`.
- APTO / NO_APTO classification is resolved inside `SuggestTickersService` from the persisted `Stock` and its `StrategyEvaluation`.
- The old classes were removed:
  - `AddSuggestedTickersToAnalysisService`
  - `AddSuggestedTickersToAnalysisUseCase`
  - `DefaultDeterministicTickerEvaluator`
  - `DeterministicTickerEvaluator`
  - `DeterministicTickerEvaluation`
- `StrategyController` no longer depends on the removed add-suggested use case. The add/refresh endpoints now switch stock `origin` through `StockDataRepository`.
- `StrategyController` keeps the analysis view filtered by `origin` so strategy-derived stocks do not appear there.

## Technical decisions
- The shared service remains the single place for quote retrieval, historical enrichment, candle persistence, company-profile validation, and strategy evaluation.
- Suggestion classification is now derived from the persisted deterministic result instead of a separate evaluator abstraction.
- The `origin` field is the control point for visibility and movement between suggestion and analysis contexts.

## Tests
- Updated unit tests for `SuggestTickersService` to mock `AnalyzeAndPersistStockService` and assert APTO / NO_APTO classification from persisted evaluations.
- Updated controller unit and web tests to reflect the origin-switch behavior.
- Verified affected tests:
  - `SuggestTickersServiceTest`
  - `StrategyControllerTest`
  - `StrategyControllerViewTest`
- Result: 28 passed, 0 failed.

## Notes and risks
- The strategy controller currently reuses the same origin-switch helper for both add and refresh actions. That keeps the UI operational, but the two endpoints now share a simplified implementation and should be reviewed if a distinct refresh semantic is needed later.
- The removed evaluator abstraction means any future deterministic classification rules should live either in `SuggestTickersService` or in a new shared domain service, not in a separate application-layer helper.

## Next steps
- Review the strategy detail view copy if the refresh/add wording should be adjusted to match the simplified behavior.
- Run a broader test suite if additional flows depend on the old suggestion-add use case.
