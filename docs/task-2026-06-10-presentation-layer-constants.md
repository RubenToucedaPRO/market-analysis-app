# Step 3 — Presentation Layer Constants

## Date
2026-06-10

## Summary
Expanded `WebConstants.java` with all presentation-layer constants and refactored all controllers, GlobalExceptionHandler, and UiNotification to use them. All hardcoded strings in controllers now resolve via `MessageSource`.

## Changes Made

### WebConstants.java (29 constants)
- **Model Attributes (14):** `ATTR_RULE_DEFINITIONS`, `ATTR_RULE_DEFINITION`, `ATTR_IS_EDIT`, `ATTR_CAPABILITIES`, `ATTR_STRATEGIES`, `ATTR_STRATEGY`, `ATTR_TICKERS`, `ATTR_TICKER`, `ATTR_SUGGESTED_TICKERS`, `ATTR_DISCARDED_TICKERS`, `ATTR_UNMAPPABLE_RULES`, `ATTR_SUGGESTED_AT`, `ATTR_PROHIBITED_TICKERS`, `ATTR_PROHIBITED_KEYWORDS`
- **Flash Attributes (1):** `UI_NOTIFICATION_KEY` (existing)
- **Template Names (10):** `TEMPLATE_ANALYSIS`, `TEMPLATE_TICKER_DETAIL`, `TEMPLATE_TICKER_CHART`, `TEMPLATE_STRATEGIES_LIST`, `TEMPLATE_STRATEGIES_CREATE`, `TEMPLATE_STRATEGIES_DETAIL`, `TEMPLATE_RULE_DEFINITIONS_LIST`, `TEMPLATE_RULE_DEFINITIONS_CREATE`, `TEMPLATE_PROHIBITED_TICKERS_LIST`, `TEMPLATE_ERROR`
- **Redirect URLs (5):** `REDIRECT_ANALYSIS`, `REDIRECT_STRATEGIES`, `REDIRECT_STRATEGIES_PREFIX`, `REDIRECT_RULE_DEFINITIONS`, `REDIRECT_PROHIBITED_TICKERS`
- **Error View Attributes (4):** `ATTR_ERROR_MESSAGE`, `ATTR_ERROR_DETAILS`, `ATTR_ERROR_TYPE`, `DEFAULT_REFERER`

### UiNotification.java
- Added `TYPE_SUCCESS`, `TYPE_DANGER`, `TYPE_WARNING` constants (replaces hardcoded `"success"`, `"danger"`, `"warning"`)

### Controller Refactoring (5 controllers)
All controllers now inject `MessageSource` and resolve flash messages via `messageSource.getMessage(key, params, locale)`:
- **RuleDefinitionController** — 3 hardcoded messages → `ruledefinition.created`, `ruledefinition.updated`, `ruledefinition.deleted`
- **ProhibitedTickerController** — Removed local `REDIRECT_PROHIBITED_TICKERS` constant; uses `WebConstants`. Added `prohibited.tickers.removed` key for ticker deletion.
- **StrategyController** — Removed 8 local constants (`ATTR_*`, `STRATEGY_REDIRECT_PREFIX`, `MSG_ADD_SNAPSHOT_NONE`); uses `WebConstants`. 9 hardcoded messages → i18n keys.
- **AnalyzeTickerController** — Removed local `REDIRECT_ANALYZE` constant; uses `WebConstants`. 6 hardcoded messages → i18n keys.
- **HealthCheckController** — Already used `HealthStatusCode` enum from Step 2.

### GlobalExceptionHandler
- Removed local constants `ERROR_VIEW`, `ATTR_ERROR_MESSAGE`, `ATTR_ERROR_DETAILS`, `ATTR_ERROR_TYPE`, `DEFAULT_REFERER` — now uses `WebConstants.*`
- `req.getHeader("Referer")` → `req.getHeader(HttpHeaders.REFERER)` (Spring constant)

### messages.properties
- Added `prohibited.tickers.removed=Ticker ''{0}'' desbloqueado y eliminado correctamente.`
- Fixed `strategy.suggestion.none_added` encoding (`anadir` → `añadir`)

### Test Updates (5 test files)
All controller tests updated to mock `MessageSource` and verify resolved messages:
- `StrategyControllerTest` — Added `@Mock MessageSource`, updated constructor, mocked `getMessage()` for 6 test methods
- `RuleDefinitionControllerTest` — Added `@Mock MessageSource`, mocked `getMessage()` for 3 test methods
- `AnalyzeTickerControllerTest` — Added `@Mock MessageSource`, mocked `getMessage()` for 5 test methods
- `ProhibitedTickerControllerTest` — `@WebMvcTest` auto-configures `MessageSource` from `messages.properties`; no changes needed
- `StrategyControllerViewTest` — `@WebMvcTest` auto-configures `MessageSource` from `messages.properties`; no changes needed

## Verification
- **1038 tests pass, 0 failures**
- `BUILD SUCCESS`

## Key Decisions
- Flash messages use `MessageSource` (i18n) instead of hardcoded strings — enables future locale switching
- `WebConstants` is the single source of truth for all presentation-layer magic strings
- `HttpHeaders.REFERER` from Spring replaces custom `"Referer"` string
- `@WebMvcTest` tests auto-resolve messages from `messages.properties` — no mocking needed

## Next Steps
- **Step 4: Infrastructure Layer** — Create `ApiConstants.java`, refactor adapters
