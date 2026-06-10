# Plan de Extracción de Constantes - Todas las Capas

## Objetivo
Identificar todos los strings hardcodeados en todas las capas del programa que deberían convertirse a constantes y concentrarse en ficheros Java de constantes, aplicando principios de diseño robusto (Enums tipados, i18n, reutilización de frameworks).

---

## Estado de Implementación

| Estado | Descripción |
|--------|-------------|
| 🔴 Pendiente | No implementado |
| 🟡 En progreso | Parcialmente implementado |
| 🟢 Completado | Implementado y verificado |

| Categoría | Estado | Notas |
|-----------|--------|-------|
| Enums de Dominio (IndicatorCode, HealthStatusCode, EvaluationStatus) | 🔴 Pendiente | Ningún enum creado aún |
| DomainValidationException | 🔴 Pendiente | Excepción de negocio no creada |
| StrategyNotFoundException | 🔴 Pendiente | Excepción de negocio no creada |
| WebConstants.java (expansión) | 🔴 Pendiente | Solo contiene `UI_NOTIFICATION_KEY` |
| messages.properties (i18n) | 🔴 Pendiente | Solo tiene 7 líneas de keywords prohibidas |
| ApiConstants.java (Infrastructure) | 🔴 Pendiente | Fichero no creado |
| Refactorización de Controllers | 🔴 Pendiente | Ningún controller refactorizado |
| Refactorización de Domain Services | 🔴 Pendiente | Ningún servicio refactorizado |
| Refactorización de Infrastructure Adapters | 🔴 Pendiente | Ningún adaptador refactorizado |
| GlobalExceptionHandler (handlers para DomainValidationException) | 🔴 Pendiente | Solo maneja excepciones existentes |

**Progreso general: ~0% - Solo existe el documento de planificación**

---

## Ficheros de Constantes Recomendados

| Fichero | Propósito | Capa |
|---------|-----------|------|
| `presentation/util/WebConstants.java` | Model attributes, template names, redirect URLs | Presentation (existente) |
| `domain/model/IndicatorCode.java` | Enum de códigos de indicadores técnicos (PRICE, SMA, EMA, etc.) | Domain (nuevo) |
| `domain/model/HealthStatusCode.java` | Enum de estados de salud (UP, DOWN, DEGRADED) | Domain (nuevo) |
| `domain/model/EvaluationStatus.java` | Enum de estados de evaluación (PASSED, FAILED) | Domain (nuevo) |
| `domain/exception/DomainValidationException.java` | Excepción de negocio fuertemente tipada con clave de error y parámetros | Domain (nuevo) |
| `src/main/resources/messages.properties` | Mensajes de error/éxitos para internacionalización i18n | Resources (nuevo) |
| `infrastructure/ApiConstants.java` | Headers HTTP, query params, paths de API | Infrastructure (nuevo) |

---

## 1. CAPA PRESENTATION

### 1.1. RuleDefinitionController.java
**Ruta:** `src/main/java/com/market/analysis/presentation/controller/RuleDefinitionController.java`

| Línea | String/Variable | Tipo | Constante Sugerida |
|-------|-----------------|------|-------------------|
| 36 | `"ruleDefinitions"` | Model attribute | `ATTR_RULE_DEFINITIONS` |
| 42 | `"ruleDefinition"` | Model attribute | `ATTR_RULE_DEFINITION` |
| 43 | `"isEdit"` | Model attribute | `ATTR_IS_EDIT` |
| 44 | `"capabilities"` | Model attribute | `ATTR_CAPABILITIES` |
| 37 | `"rule-definitions/list"` | Template name | `VIEW_RULE_DEFINITIONS_LIST` |
| 45, 53 | `"rule-definitions/create"` | Template name | `VIEW_RULE_DEFINITIONS_CREATE` |
| 70, 78 | `"redirect:/rule-definitions"` | Redirect URL | `REDIRECT_RULE_DEFINITIONS` |

**Mensajes a migrar a `messages.properties`:**
- `ruledefinition.created=Definición de regla creada correctamente.`
- `ruledefinition.updated=Definición de regla actualizada correctamente.`
- `ruledefinition.deleted=Definición de regla eliminada con éxito.`

---

### 1.2. ProhibitedTickerController.java
**Ruta:** `src/main/java/com/market/analysis/presentation/controller/ProhibitedTickerController.java`

| Línea | String/Variable | Tipo | Constante Sugerida |
|-------|-----------------|------|-------------------|
| 36 | `"redirect:/prohibited-tickers"` | Redirect URL | `REDIRECT_PROHIBITED_TICKERS` ✅ (ya existe) |
| 47 | `"prohibitedTickers"` | Model attribute | `ATTR_PROHIBITED_TICKERS` |
| 48 | `"prohibitedKeywords"` | Model attribute | `ATTR_PROHIBITED_KEYWORDS` |
| 49 | `"prohibited-tickers/list"` | Template name | `VIEW_PROHIBITED_TICKERS_LIST` |

**Nota:** Usa `messageSource` para mensajes internacionalizados ✓

---

### 1.3. StrategyController.java
**Ruta:** `src/main/java/com/market/analysis/presentation/controller/StrategyController.java`

**Constantes ya definidas localmente (mover a WebConstants):**
| Constante Local | Valor | Constante Global Sugerida |
|----------------|-------|--------------------------|
| `STRATEGY_REDIRECT_PREFIX` | `"redirect:/strategies/"` | `REDIRECT_STRATEGIES_PREFIX` |
| `ATTR_RULE_DEFINITIONS` | `"ruleDefinitions"` | `ATTR_RULE_DEFINITIONS` |
| `ATTR_STRATEGY` | `"strategy"` | `ATTR_STRATEGY` |
| `ATTR_SUGGESTED_TICKERS` | `"suggestedTickers"` | `ATTR_SUGGESTED_TICKERS` |
| `ATTR_DISCARDED_TICKERS` | `"discardedTickers"` | `ATTR_DISCARDED_TICKERS` |
| `ATTR_UNMAPPABLE_RULES` | `"unmappableRules"` | `ATTR_UNMAPPABLE_RULES` |
| `ATTR_SUGGESTED_AT` | `"suggestedAt"` | `ATTR_SUGGESTED_AT` |

**Model attributes hardcodeados:**
| Línea | String/Variable | Constante Sugerida |
|-------|-----------------|-------------------|
| 55 | `"strategies"` | `ATTR_STRATEGIES` |
| 84, 97 | `"isEdit"` | `ATTR_IS_EDIT` |

**Template names:**
| Línea | String | Constante Sugerida |
|-------|--------|-------------------|
| 56 | `"strategies/list"` | `VIEW_STRATEGIES_LIST` |
| 64 | `"strategies/detail"` | `VIEW_STRATEGIES_DETAIL` |
| 86, 99 | `"strategies/create"` | `VIEW_STRATEGIES_CREATE` |

**Redirect URLs:**
| Línea | String | Constante Sugerida |
|-------|--------|-------------------|
| 113, 121 | `"redirect:/strategies"` | `REDIRECT_STRATEGIES` |
| 177 | `"redirect:/analysis"` | `REDIRECT_ANALYSIS` |

**Mensajes a migrar a `messages.properties`:**
- `strategy.created=Estrategia creada correctamente.`
- `strategy.updated=Estrategia actualizada correctamente.`
- `strategy.deleted=Estrategia eliminada correctamente.`
- `strategy.suggestion.unavailable=La sugerencia de tickers desde mercado no está disponible todavía.`
- `strategy.suggestion.failed=No se pudo sugerir tickers desde mercado en este momento.`
- `strategy.suggestion.partial=Sugerencia parcial: revisa trazabilidad de descartes o reglas no mapeables.`
- `strategy.suggestion.success=Sugerencias generadas correctamente desde mercado.`
- `strategy.tickers.switched=Ticker(s) cambiados a origen análisis: {0}.`
- `strategy.suggestion.none_added=No hay sugerencias aptas en snapshot para añadir.`

---

### 1.4. AnalyzeTickerController.java
**Ruta:** `src/main/java/com/market/analysis/presentation/controller/AnalyzeTickerController.java`

**Constantes ya definidas localmente (mover a WebConstants):**
| Constante Local | Valor | Constante Global Sugerida |
|----------------|-------|--------------------------|
| `REDIRECT_ANALYZE` | `"redirect:/analysis"` | `REDIRECT_ANALYZE` |

**Model attributes hardcodeados:**
| Línea | String | Constante Sugerida |
|-------|--------|-------------------|
| 41 | `"tickers"` | `ATTR_TICKERS` |
| 42 | `"strategies"` | `ATTR_STRATEGIES` |
| 86 | `"ticker"` | `ATTR_TICKER` |

**Template names:**
| Línea | String | Constante Sugerida |
|-------|--------|-------------------|
| 43 | `"analysis/analysis"` | `VIEW_ANALYSIS_LIST` |
| 87 | `"analysis/ticker-detail"` | `VIEW_TICKER_DETAIL` |
| 109 | `"analysis/ticker-chart"` | `VIEW_TICKER_CHART` |

**Mensajes a migrar a `messages.properties`:**
- `ticker.added=Ticker(s) añadidos y analizados correctamente.`
- `ticker.updated=Datos del ticker actualizados correctamente.`
- `ticker.deleted=Ticker ''{0}'' eliminado correctamente.`
- `ticker.ia.success=Valoración IA generada y guardada correctamente.`
- `ticker.ia.failed=No se pudo generar una valoración IA válida. Se guardó un mensaje de fallback.`
- `ticker.strategy.required=Strategy selection is required`

---

### 1.5. HealthCheckController.java
**Ruta:** `src/main/java/com/market/analysis/presentation/controller/HealthCheckController.java`

| Línea | String | Tipo | Constante Sugerida |
|-------|--------|------|-------------------|
| 23 | `"/health"` | RequestMapping | `PATH_HEALTH` |
| 47 | `"UP"` | Status value | `HealthStatusCode.UP` (Enum) |

---

### 1.6. GlobalExceptionHandler.java
**Ruta:** `src/main/java/com/market/analysis/presentation/exception/GlobalExceptionHandler.java`

**Constantes ya definidas localmente (mover a WebConstants):**
| Constante Local | Valor | Constante Global Sugerida |
|----------------|-------|--------------------------|
| `ERROR_VIEW` | `"error"` | `VIEW_ERROR` |
| `ATTR_ERROR_MESSAGE` | `"errorMessage"` | `ATTR_ERROR_MESSAGE` |
| `ATTR_ERROR_DETAILS` | `"errorDetails"` | `ATTR_ERROR_DETAILS` |
| `ATTR_ERROR_TYPE` | `"errorType"` | `ATTR_ERROR_TYPE` |
| `DEFAULT_REFERER` | `"/"` | `DEFAULT_REFERER` |

**Mensajes a migrar a `messages.properties`:**
- `error.missing_indicators=Faltan datos de indicadores técnicos para realizar el análisis.`
- `error.rule_not_evaluable=La regla configurada no puede ser evaluada. Verifica la configuración.`
- `error.invalid_params=Se proporcionaron parámetros inválidos. Por favor, verifica e intenta de nuevo.`
- `error.illegal_state=Error de estado interno. Por favor, contacta con soporte.`
- `error.external_service=El servicio de datos de mercado no está disponible temporalmente`
- `error.entity_in_use=No se puede eliminar el recurso porque tiene dependencias asociadas`
- `error.database=A database error occurred while processing your request.`
- `error.unexpected=An unexpected system error occurred. Please try again later.`

**Referencia HTTP Header:**
- En la línea 248 (`req.getHeader("Referer")`), eliminar la sugerencia de constante propia y usar directamente la constante nativa de Spring: `org.springframework.http.HttpHeaders.REFERER`

**⚠️ IMPORTANTE: Manejo de DomainValidationException:**
- Añadir handler para `DomainValidationException` que use `MessageSource` para traducir la clave del error
- El dominio lanza la excepción con clave + parámetros; la presentation resuelve el mensaje final

---

### 1.7. UiNotification.java
**Ruta:** `src/main/java/com/market/analysis/presentation/dto/UiNotification.java`

| Línea | String | Tipo | Constante Sugerida |
|-------|--------|------|-------------------|
| 22 | `"success"` | Bootstrap alert type | `TYPE_SUCCESS` |
| 32 | `"danger"` | Bootstrap alert type | `TYPE_DANGER` |
| 42 | `"warning"` | Bootstrap alert type | `TYPE_WARNING` |

---

## 2. CAPA DOMAIN

> **⚠️ Regla de Arquitectura Hexónica (Clean Architecture):**
> El dominio debe ser **Java puro**, sin dependencias de frameworks de infraestructura (Spring MessageSource, i18n, etc.).
> Cuando salte un error en el dominio, se lanza una **excepción de negocio fuertemente tipada** (ej. `DomainValidationException`) con una **clave de error** y **parámetros limpios**.
> El `GlobalExceptionHandler` de la capa de Presentation captura la excepción, inyecta `MessageSource` y resuelve el mensaje final usando el código de `messages.properties`.

---

### 2.1. RuleEvaluator.java
**Ruta:** `src/main/java/com/market/analysis/domain/service/RuleEvaluator.java`

| Línea | String | Tipo | Constante Sugerida |
|-------|--------|------|-------------------|
| 98 | `"GREATER_THAN"` | Operator alias | `OperatorConstants.GREATER_THAN` |
| 99 | `"GREATER_THAN_OR_EQUAL"` | Operator alias | `OperatorConstants.GREATER_THAN_OR_EQUAL` |
| 100 | `"LESS_THAN"` | Operator alias | `OperatorConstants.LESS_THAN` |
| 101 | `"LESS_THAN_OR_EQUAL"` | Operator alias | `OperatorConstants.LESS_THAN_OR_EQUAL` |
| 102 | `"EQUALS"` | Operator alias | `OperatorConstants.EQUALS` |
| 103 | `"NOT_EQUALS"` | Operator alias | `OperatorConstants.NOT_EQUALS` |
| 113 | `"PASSED"` / `"FAILED"` | Status value | `EvaluationStatus.PASSED` / `EvaluationStatus.FAILED` (Enum) |
| 128 | `"FAILED: Missing both subject and target data"` | Justification | `MSG_MISSING_BOTH_DATA` |
| 130 | `"FAILED: Missing subject data for "` | Justification | `MSG_MISSING_SUBJECT_DATA` |
| 133 | `"FAILED: Missing target data for "` | Justification | `MSG_MISSING_TARGET_DATA` |
| 143 | `"UNKNOWN"` | Indicator label | `IndicatorCode.UNKNOWN` (Enum) |
| 151 | `"SMA"` | Indicator code | `IndicatorCode.SMA` (Enum) |
| 152 | `"EMA"` | Indicator code | `IndicatorCode.EMA` (Enum) |
| 153 | `"RSI"` | Indicator code | `IndicatorCode.RSI` (Enum) |
| 154 | `"CONSTANT"` | Indicator code | `IndicatorCode.CONSTANT` (Enum) |

---

### 2.2. RiskRewardCalculator.java
**Ruta:** `src/main/java/com/market/analysis/domain/service/RiskRewardCalculator.java`

**❌ NO usar `messages.properties` directamente en dominio. Lanzar `DomainValidationException` con clave + parámetros.**

| Línea | String | Tipo | Acción en Dominio | Clave en `messages.properties` |
|-------|--------|------|-------------------|-------------------------------|
| 44 | `"Strategy objective cannot be null"` | Error | `throw new DomainValidationException("validation.strategy_objective_null")` | `validation.strategy_objective_null=Strategy objective cannot be null` |
| 45 | `"Stock cannot be null"` | Error | `throw new DomainValidationException("validation.stock_null")` | `validation.stock_null=Stock cannot be null` |
| 50 | `"target"` | Context description | `CONTEXT_TARGET` (constante local) | — |
| 52 | `"Target"` | Context display | `CONTEXT_TARGET_DISPLAY` (constante local) | — |
| 75 | `"stop-loss"` | Context description | `CONTEXT_STOP_LOSS` (constante local) | — |
| 77 | `"Stop-loss"` | Context display | `CONTEXT_STOP_LOSS_DISPLAY` (constante local) | — |
| 97 | `"Target price cannot be null"` | Error | `throw new DomainValidationException("validation.target_price_null")` | `validation.target_price_null=Target price cannot be null` |
| 98 | `"Stop price cannot be null"` | Error | `throw new DomainValidationException("validation.stop_price_null")` | `validation.stop_price_null=Stop price cannot be null` |
| 101 | `"Target price"` | Field name | `FIELD_TARGET_PRICE` (constante local) | — |
| 102 | `"Stop price"` | Field name | `FIELD_STOP_PRICE` (constante local) | — |
| 106 | `"Target price must be greater than entry price..."` | Error | `throw new DomainValidationException("validation.target_below_entry")` | `validation.target_below_entry=Target price must be greater than entry price for long positions` |
| 109 | `"Stop price must be less than entry price..."` | Error | `throw new DomainValidationException("validation.stop_above_entry")` | `validation.stop_above_entry=Stop price must be less than entry price for long positions` |
| 116 | `"Potential risk must be greater than zero"` | Error | `throw new DomainValidationException("validation.risk_zero")` | `validation.risk_zero=Potential risk must be greater than zero` |
| 136 | `"Capital to risk cannot be null"` | Error | `throw new DomainValidationException("validation.capital_null")` | `validation.capital_null=Capital to risk cannot be null` |
| 140 | `"Capital to risk"` | Field name | `FIELD_CAPITAL_TO_RISK` (constante local) | — |
| 149 | `"Risk per share must be greater than zero"` | Error | `throw new DomainValidationException("validation.risk_per_share_zero")` | `validation.risk_per_share_zero=Risk per share must be greater than zero` |
| 171 | `"SMA period value cannot be null"` | Error | `throw new DomainValidationException("validation.sma_period_null")` | `validation.sma_period_null=SMA period value cannot be null` |
| 175, 181, 188 | `"SMA"` | Indicator code | `IndicatorCode.SMA` (Enum) | — |
| 211 | `"Percentage value cannot be null"` | Error | `throw new DomainValidationException("validation.percentage_null")` | `validation.percentage_null=Percentage value cannot be null` |
| 214 | `"Percentage value must be greater than zero"` | Error | `throw new DomainValidationException("validation.percentage_zero")` | `validation.percentage_zero=Percentage value must be greater than zero` |

---

### 2.3. EvaluateStrategyService.java
**Ruta:** `src/main/java/com/market/analysis/domain/service/EvaluateStrategyService.java`

**❌ NO usar `messages.properties` directamente en dominio. Lanzar `DomainValidationException` con clave + parámetros.**

| Línea | String | Tipo | Acción en Dominio | Clave en `messages.properties` |
|-------|--------|------|-------------------|-------------------------------|
| 43 | `"Strategy cannot be null"` | Error | `throw new DomainValidationException("validation.strategy_null")` | `validation.strategy_null=Strategy cannot be null` |
| 46 | `"Stock data cannot be null"` | Error | `throw new DomainValidationException("validation.stock_data_null")` | `validation.stock_data_null=Stock data cannot be null` |
| 97 | `" Risk plan could not be calculated: "` | Summary text | `MSG_RISK_PLAN_FAILED` (constante local) | — |
| 127 | `"totalRules"` | Metric key | `METRIC_TOTAL_RULES` (constante local) | — |
| 128 | `"passedRules"` | Metric key | `METRIC_PASSED_RULES` (constante local) | — |
| 129 | `"failedRules"` | Metric key | `METRIC_FAILED_RULES` (constante local) | — |
| 151 | `"Strategy '%s' evaluation for %s: %s. "` | Summary template | `SUMMARY_TEMPLATE` (constante local) | — |
| 155 | `"%d/%d rules passed."` | Summary template | `RULES_PASSED_TEMPLATE` (constante local) | — |
| 158 | `" Failed rules: "` | Summary suffix | `SUFFIX_FAILED_RULES` (constante local) | — |

---

### 2.4. PromptBuilder.java
**Ruta:** `src/main/java/com/market/analysis/domain/service/PromptBuilder.java`

**❌ NO usar `messages.properties` directamente en dominio. Lanzar `DomainValidationException` con clave + parámetros.**

| Línea | String | Tipo | Acción en Dominio | Clave en `messages.properties` |
|-------|--------|------|-------------------|-------------------------------|
| 12 | `"N/A"` | Display value | `NOT_AVAILABLE` (constante local) | — |
| 15 | `"Stock cannot be null"` | Error | `throw new DomainValidationException("validation.stock_null")` | `validation.stock_null=Stock cannot be null` |
| 19-35 | (entire prompt template) | Prompt template | `PROMPT_TEMPLATE` (constante local) | — |

---

### 2.5. PromptResponseValidator.java
**Ruta:** `src/main/java/com/market/analysis/domain/service/PromptResponseValidator.java`

| Línea | String | Tipo | Constante Sugerida |
|-------|--------|------|-------------------|
| 9 | `"Resumen tecnico:"` | Section marker | `SECTION_RESUMEN_TECNICO` |
| 10 | `"Fortalezas:"` | Section marker | `SECTION_FORTALEZAS` |
| 11 | `"Riesgos:"` | Section marker | `SECTION_RIESGOS` |
| 12 | `"Conclusion interpretativa:"` | Section marker | `SECTION_CONCLUSION` |
| 14-22 | (strict retry suffix) | Prompt template | `STRICT_RETRY_SUFFIX` |

---

### 2.6. FinvizFilterMapperImpl.java
**Ruta:** `src/main/java/com/market/analysis/domain/service/FinvizFilterMapperImpl.java`

**❌ NO usar `messages.properties` directamente en dominio. Lanzar `DomainValidationException` o retornar warning como DTO.**

| Línea | String | Tipo | Acción en Dominio | Clave en `messages.properties` |
|-------|--------|------|-------------------|-------------------------------|
| 20 | `"VOLUME"`, `"AVG_VOLUME"` | Indicator codes | `IndicatorCode.VOLUME` / `IndicatorCode.AVG_VOLUME` (Enum) | — |
| 21 | `"CONSTANT"`, `"VALUE"` | Indicator codes | `IndicatorCode.CONSTANT` / `IndicatorCode.VALUE` (Enum) | — |
| 23-27 | `">"`, `"<"`, `"GREATER_THAN"`, `"LESS_THAN"` | Operator aliases | `OperatorConstants.GT` / `LT` etc. | — |
| 30-53 | All Finviz filter codes | Filter codes | `FinvizFilterCodes.*` (constante local) | — |
| 78 | `"NULL_RULE"` | Label | `NULL_RULE_LABEL` (constante local) | — |
| 79 | `"Rule 'NULL_RULE' cannot be mapped..."` | Warning | Retornar DTO con `warningCode = "finviz.null_rule"` | `finviz.null_rule=Rule ''NULL_RULE'' cannot be mapped to Finviz filters.` |
| 87 | `"Rule '"` / `"' cannot be mapped..."` | Warning | Retornar DTO con `warningCode = "finviz.rule_unmappable"` + parámetros | `finviz.rule_unmappable=Rule ''{0}'' cannot be mapped to Finviz filters.` |
| 165 | `"UNKNOWN"` | Indicator label | `IndicatorCode.UNKNOWN` (Enum) | — |

---

### 2.7. RuleCapabilityCatalog.java
**Ruta:** `src/main/java/com/market/analysis/domain/model/RuleCapabilityCatalog.java`

| Línea | String | Constante Sugerida |
|-------|--------|-------------------|
| 36 | `"PRICE"` | `IndicatorCode.PRICE` (Enum) |
| 40 | `"SMA"` | `IndicatorCode.SMA` (Enum) |
| 45 | `"EMA"` | `IndicatorCode.EMA` (Enum) |
| 50 | `"RSI"` | `IndicatorCode.RSI` (Enum) |
| 55 | `"MACD_LINE"` | `IndicatorCode.MACD_LINE` (Enum) |
| 59 | `"MACD_SIGNAL"` | `IndicatorCode.MACD_SIGNAL` (Enum) |
| 63 | `"MACD_HIST"` | `IndicatorCode.MACD_HIST` (Enum) |
| 67 | `"BB_UPPER"` | `IndicatorCode.BB_UPPER` (Enum) |
| 72 | `"BB_LOWER"` | `IndicatorCode.BB_LOWER` (Enum) |
| 77 | `"ATR"` | `IndicatorCode.ATR` (Enum) |
| 82 | `"VOLUME"` | `IndicatorCode.VOLUME` (Enum) |
| 86 | `"AVG_VOLUME"` | `IndicatorCode.AVG_VOLUME` (Enum) |
| 90 | `"OPEN"` | `IndicatorCode.OPEN` (Enum) |
| 94 | `"HIGH"` | `IndicatorCode.HIGH` (Enum) |
| 98 | `"LOW"` | `IndicatorCode.LOW` (Enum) |
| 102 | `"PREV_CLOSE"` | `IndicatorCode.PREV_CLOSE` (Enum) |
| 106 | `"CONSTANT"` | `IndicatorCode.CONSTANT` (Enum) |
| 110 | `"VALUE"` | `IndicatorCode.VALUE` (Enum) |

---

### 2.8. StrategyObjective.java
**Ruta:** `src/main/java/com/market/analysis/domain/model/StrategyObjective.java`

**❌ NO usar `messages.properties` directamente en dominio. Lanzar `DomainValidationException` con clave + parámetros.**

| Línea | String | Tipo | Acción en Dominio | Clave en `messages.properties` |
|-------|--------|------|-------------------|-------------------------------|
| 69 | `"targetType cannot be null"` | Validation | `throw new DomainValidationException("validation.target_type_null")` | `validation.target_type_null=targetType cannot be null` |
| 72 | `"stopLossType cannot be null"` | Validation | `throw new DomainValidationException("validation.stop_loss_type_null")` | `validation.stop_loss_type_null=stopLossType cannot be null` |
| 75 | `"targetValue cannot be null"` | Validation | `throw new DomainValidationException("validation.target_value_null")` | `validation.target_value_null=targetValue cannot be null` |
| 78 | `"stopLossValue cannot be null"` | Validation | `throw new DomainValidationException("validation.stop_loss_value_null")` | `validation.stop_loss_value_null=stopLossValue cannot be null` |
| 81 | `"capitalToRisk cannot be null"` | Validation | `throw new DomainValidationException("validation.capital_to_risk_null")` | `validation.capital_to_risk_null=capitalToRisk cannot be null` |
| 84 | `"description cannot be null or blank"` | Validation | `throw new DomainValidationException("validation.description_null")` | `validation.description_null=description cannot be null or blank` |
| 88 | `"targetValue must be greater than zero"` | Validation | `throw new DomainValidationException("validation.target_value_zero")` | `validation.target_value_zero=targetValue must be greater than zero` |
| 91 | `"stopLossValue must be greater than zero"` | Validation | `throw new DomainValidationException("validation.stop_loss_value_zero")` | `validation.stop_loss_value_zero=stopLossValue must be greater than zero` |
| 94 | `"capitalToRisk must be greater than zero"` | Validation | `throw new DomainValidationException("validation.capital_to_risk_zero")` | `validation.capital_to_risk_zero=capitalToRisk must be greater than zero` |
| 114 | `"SMA"` | Indicator code | `IndicatorCode.SMA` (Enum) | — |

---

### 2.9. Strategy.java
**Ruta:** `src/main/java/com/market/analysis/domain/model/Strategy.java`

**❌ NO usar `messages.properties` directamente en dominio. Lanzar `DomainValidationException` con clave + parámetros.**

| Línea | String | Tipo | Acción en Dominio | Clave en `messages.properties` |
|-------|--------|------|-------------------|-------------------------------|
| 73 | `"Strategy name cannot be null or empty"` | Validation | `throw new DomainValidationException("validation.strategy_name_null")` | `validation.strategy_name_null=Strategy name cannot be null or empty` |
| 76 | `"Strategy description cannot be null or empty"` | Validation | `throw new DomainValidationException("validation.strategy_desc_null")` | `validation.strategy_desc_null=Strategy description cannot be null or empty` |
| 79 | `"Strategy must contain at least one rule"` | Validation | `throw new DomainValidationException("validation.strategy_no_rules")` | `validation.strategy_no_rules=Strategy must contain at least one rule` |
| 85 | `"Strategy cannot contain null rules"` | Validation | `throw new DomainValidationException("validation.strategy_null_rule")` | `validation.strategy_null_rule=Strategy cannot contain null rules` |
| 91 | `"Strategy objective cannot be null"` | Validation | `throw new DomainValidationException("validation.strategy_objective_null")` | `validation.strategy_objective_null=Strategy objective cannot be null` |

---

## 3. CAPA APPLICATION

### 3.1. AnalyzeAndPersistStockService.java
**Ruta:** `src/main/java/com/market/analysis/application/usecase/AnalyzeAndPersistStockService.java`

| Línea | String | Tipo | Constante Sugerida |
|-------|--------|------|-------------------|
| 39 | `"America/New_York"` | Time zone | `NEW_YORK_ZONE_ID` |
| 83 | `"PASSED"` / `"FAILED"` | Status value | `EvaluationStatus.PASSED` / `EvaluationStatus.FAILED` (Enum) |

---

### 3.2. ManageAnalyzeStockService.java
**Ruta:** `src/main/java/com/market/analysis/application/usecase/ManageAnalyzeStockService.java`

| Línea | String | Tipo | Acción | Clave en `messages.properties` |
|-------|--------|------|--------|-------------------------------|
| 36 | `"Ticker data not found for: "` | Error | `throw new TickerNotFoundException("ticker.not_found", ticker)` | `ticker.not_found=Ticker data not found for: {0}` |
| 37 | `"No se pudo generar una valoracion..."` | Fallback | Retornar fallback con clave `ticker.ia.fallback` | `ticker.ia.fallback=No se pudo generar una valoracion interpretativa valida...` |
| 59 | `"Strategy ID is required"` | Error | `throw new DomainValidationException("validation.strategy_id_required")` | `validation.strategy_id_required=Strategy ID is required` |
| 64 | `"Strategy not found with id: "` | Error | `throw new StrategyNotFoundException("strategy.not_found", strategyId)` | `strategy.not_found=Strategy not found with id: {0}` |
| 148 | `"initial"` | Stage name | `STAGE_INITIAL` (constante local) | — |
| 175 | `"retry"` | Stage name | `STAGE_RETRY` (constante local) | — |
| 196 | `"Prompt cannot be null"` | Error | `throw new DomainValidationException("validation.prompt_null")` | `validation.prompt_null=Prompt cannot be null` |

---

### 3.3. SuggestTickersService.java
**Ruta:** `src/main/java/com/market/analysis/application/usecase/SuggestTickersService.java`

| Línea | String | Tipo | Acción | Clave en `messages.properties` |
|-------|--------|------|--------|-------------------------------|
| 37 | `"No Finviz filters..."` | Warning | Retornar warning con clave `strategy.empty_filters` | `strategy.empty_filters=No Finviz filters could be generated for this strategy.` |
| 39 | `"Finviz no esta disponible..."` | Warning | Retornar warning con clave `strategy.finviz_degraded` | `strategy.finviz_degraded=Finviz no esta disponible temporalmente...` |
| 55 | `"Strategy not found with id: "` | Error | `throw new StrategyNotFoundException("strategy.not_found", strategyId)` | `strategy.not_found=Strategy not found with id: {0}` |
| 157 | `"No se pudo generar..."` | Traceability | Retornar DTO con clave `strategy.evaluation_failed` | `strategy.evaluation_failed=No se pudo generar una evaluacion determinista valida.` |
| 162 | `"Ticker apto"` / `"Ticker no apto"` | Traceability | Retornar DTO con clave `ticker.apto` / `ticker.no_apto` | `ticker.apto=Ticker apto` / `ticker.no_apto=Ticker no apto` |
| 248 | `"Suggest tickers request cannot be null"` | Error | `throw new DomainValidationException("validation.request_null")` | `validation.request_null=Suggest tickers request cannot be null` |
| 251 | `"Strategy ID is required"` | Error | `throw new DomainValidationException("validation.strategy_id_required")` | `validation.strategy_id_required=Strategy ID is required` |

---

### 3.4. ManageStrategyService.java
**Ruta:** `src/main/java/com/market/analysis/application/usecase/ManageStrategyService.java`

| Línea | String | Tipo | Acción | Clave en `messages.properties` |
|-------|--------|------|--------|-------------------------------|
| 86 | `"Strategy not found with id: "` | Error | `throw new StrategyNotFoundException("strategy.not_found", strategyId)` | `strategy.not_found=Strategy not found with id: {0}` |

---

### 3.5. ManageRuleDefinitionService.java
**Ruta:** `src/main/java/com/market/analysis/application/usecase/ManageRuleDefinitionService.java`

| Línea | String | Tipo | Acción | Clave en `messages.properties` |
|-------|--------|------|--------|-------------------------------|
| 35 | `"RuleDefinition cannot be null"` | Error | `throw new DomainValidationException("validation.rd_null")` | `validation.rd_null=RuleDefinition cannot be null` |
| 39 | `"RuleDefinition code cannot be null..."` | Error | `throw new DomainValidationException("validation.rd_code_null")` | `validation.rd_code_null=RuleDefinition code cannot be null or empty` |
| 46 | `"RuleDefinition with code..."` | Error | `throw new DuplicateEntityException("validation.rd_exists", code)` | `validation.rd_exists=RuleDefinition with code ''{0}'' already exists` |
| 79 | `"RuleDefinition ID cannot be null..."` | Error | `throw new DomainValidationException("validation.rd_id_null")` | `validation.rd_id_null=RuleDefinition ID cannot be null for update` |
| 83, 96 | `"RuleDefinition not found..."` | Error | `throw new RuleDefinitionNotFoundException("ruledefinition.not_found", id)` | `ruledefinition.not_found=RuleDefinition not found with id: {0}` |
| 136-137 | `"Rule code..."` | Error | `throw new DomainValidationException("validation.rd_unsupported_code", code)` | `validation.rd_unsupported_code=Rule code ''{0}'' is not supported` |
| 147 | `"Rule code..."` | Error | `throw new DomainValidationException("validation.rd_param_conflict", code, requiredParam)` | `validation.rd_param_conflict=Rule code ''{0}'' requires requiresParam={1}` |

---

### 3.6. ManageProhibitedKeywordService.java
**Ruta:** `src/main/java/com/market/analysis/application/usecase/ManageProhibitedKeywordService.java`

| Línea | String | Tipo | Acción | Clave en `messages.properties` |
|-------|--------|------|--------|-------------------------------|
| 41 | `"Prohibited keyword cannot be null"` | Error | `throw new DomainValidationException("validation.keyword_null")` | `validation.keyword_null=Prohibited keyword cannot be null` |
| 46 | `"Prohibited keyword already exists..."` | Error | `throw new DuplicateEntityException("validation.keyword_exists", keyword)` | `validation.keyword_exists=Prohibited keyword already exists: {0}` |
| 73 | `"Keyword cannot be null or blank"` | Error | `throw new DomainValidationException("validation.keyword_blank")` | `validation.keyword_blank=Keyword cannot be null or blank` |
| 78 | `"Keyword length must be <= "` | Error | `throw new DomainValidationException("validation.keyword_too_long", maxLength)` | `validation.keyword_too_long=Keyword length must be <= {0}` |

---

### 3.7. HealthCheckService.java
**Ruta:** `src/main/java/com/market/analysis/application/usecase/HealthCheckService.java`

| Línea | String | Tipo | Constante Sugerida |
|-------|--------|------|-------------------|
| 74 | `"DOWN"` | Status value | `HealthStatusCode.DOWN` (Enum) |
| 76, 87 | `"UP"` | Status value | `HealthStatusCode.UP` (Enum) |
| 89 | `"DEGRADED"` | Status value | `HealthStatusCode.DEGRADED` (Enum) |
| 87 | `"Application is fully operational..."` | Description | Migrar a `messages.properties` → `health.app_up` |
| 88 | `"Application is not operational..."` | Description | Migrar a `messages.properties` → `health.app_down` |
| 89 | `"Application is partially operational..."` | Description | Migrar a `messages.properties` → `health.app_degraded` |
| 90 | `"Unknown status"` | Description | Migrar a `messages.properties` → `health.app_unknown` |
| 103 | `"Healthy"` / `"Unhealthy"` | Detail label | Migrar a `messages.properties` → `health.healthy` / `health.unhealthy` |

---

### 3.8. HealthCheckMapper.java
**Ruta:** `src/main/java/com/market/analysis/application/mapper/HealthCheckMapper.java`

| Línea | String | Tipo | Constante Sugerida |
|-------|--------|------|-------------------|
| 28 | `"UP"` | Status value | `HealthStatusCode.UP` (Enum) |

---

## 4. CAPA INFRASTRUCTURE

### 4.1. FinnhubAdapter.java
**Ruta:** `src/main/java/com/market/analysis/infrastructure/external/finnhub/FinnhubAdapter.java`

| Línea | String | Tipo | Constante Sugerida |
|-------|--------|------|-------------------|
| 45 | `"/quote"` | API endpoint | `ENDPOINT_QUOTE` |
| 51 | `"Limit exceeded when fetching quote for "` | Error message | Migrar a `messages.properties` → `finnhub.rate_limit` |
| 57 | `"No valid data found for: "` | Error message | Migrar a `messages.properties` → `finnhub.no_data` |
| 66 | `"API error for "` | Error message | Migrar a `messages.properties` → `finnhub.api_error` |
| 69 | `"Unexpected error fetching quote "` | Error message | Migrar a `messages.properties` → `finnhub.unexpected` |
| 80 | `"/stock/profile2"` | API endpoint | `ENDPOINT_PROFILE` |

---

### 4.2. PolygonAdapter.java
**Ruta:** `src/main/java/com/market/analysis/infrastructure/external/polygon/PolygonAdapter.java`

| Línea | String | Tipo | Constante Sugerida |
|-------|--------|------|-------------------|
| 81 | `"Error communicating with Polygon: "` | Error message | Migrar a `messages.properties` → `polygon.comm_error` |
| 83 | `"Unexpected error processing Polygon data for "` | Error message | Migrar a `messages.properties` → `polygon.unexpected` |
| 103 | `"results"` | JSON path | `JSON_RESULTS` |
| 108 | `"c"` | JSON field | `JSON_CLOSE_PRICE` |
| 109 | `"v"` | JSON field | `JSON_VOLUME` |
| 114 | `"t"` | JSON field | `JSON_TIMESTAMP` |
| 121 | `"Error parsing API response for ticker "` | Error message | Migrar a `messages.properties` → `polygon.parse_error` |
| 152 | `"v2/aggs/ticker/{ticker}/range/1/day/{from}/{to}"` | URI path | `URI_PATH_AGGREGATES` |
| 153 | `"adjusted"` / `"true"` | Query param | `QUERY_ADJUSTED` |
| 154 | `"sort"` / `"desc"` | Query param | `QUERY_SORT` / `SORT_DESC` |
| 155 | `"limit"` | Query param | `QUERY_LIMIT` |
| 156 | `"apiKey"` | Query param | `QUERY_API_KEY` |

---

### 4.3. JsoupFinvizAdapter.java
**Ruta:** `src/main/java/com/market/analysis/infrastructure/external/finviz/JsoupFinvizAdapter.java`

| Línea | String | Tipo | Constante Sugerida |
|-------|--------|------|-------------------|
| 132 | `"v"` / `"111"` | Query param | `QUERY_VIEW` / `VIEW_VALUE` |
| 133 | `"r"` | Query param | `QUERY_ROW` |
| 137 | `"f"` | Query param | `QUERY_FILTERS` |
| 150 | `"tbody tr"` | CSS selector | `SELECTOR_TABLE_ROWS` |
| 164 | `"a.tab-link, a[href*=ashx?t=]"` | CSS selector | `SELECTOR_TICKER_LINK` |

---

### 4.4. OpenrouterAdapter.java
**Ruta:** `src/main/java/com/market/analysis/infrastructure/external/openrouter/OpenrouterAdapter.java`

| Línea | String | Tipo | Constante Sugerida |
|-------|--------|------|-------------------|
| 69 | `"Error calling OpenRouter API"` | Error message | Migrar a `messages.properties` → `openrouter.error` |

---

### 4.5. BeanConfig.java
**Ruta:** `src/main/java/com/market/analysis/infrastructure/config/BeanConfig.java`

| Línea | String | Tipo | Constante Sugerida |
|-------|--------|------|-------------------|
| 205 | `"Finnhub base URL is not configured properly."` | Error message | Migrar a `messages.properties` → `config.finnhub_url_invalid` |
| 242 | `"https://openrouter.ai/api/v1"` | URL | `OPENROUTER_BASE_URL` |
| 244 | `"HTTP-Referer"` | HTTP header | Usar `HttpHeaders.REFERER` de Spring (constante nativa) |
| 244 | `"http://localhost:8080"` | URL | `DEFAULT_REFERER_URL` |

---

### 4.6. SlowQueryInspector.java
**Ruta:** `src/main/java/com/market/analysis/infrastructure/config/SlowQueryInspector.java`

| Línea | String | Tipo | Constante Sugerida |
|-------|--------|------|-------------------|
| 46 | `"SQL a ejecutar: "` | Log prefix | `LOG_SQL_PREFIX` |
| 56 | `"password"`, `"token"`, `"secret"`, `"api[_-]?key"` | Security patterns | `SENSITIVE_FIELD_PATTERNS` |
| 57 | `"$1='*****'"` | Security replacement | `OBFUSCATION_REPLACEMENT` |

---

### 4.7. ApiKeyObfuscatorInterceptor.java
**Ruta:** `src/main/java/com/market/analysis/infrastructure/config/ApiKeyObfuscatorInterceptor.java`

| Línea | String | Tipo | Constante Sugerida |
|-------|--------|------|-------------------|
| 34 | `"apikey"`, `"token"` | Regex patterns | `SENSITIVE_PARAM_PATTERNS` |
| 34 | `"$1=*****"` | Security replacement | `OBFUSCATION_REPLACEMENT` |
| 36 | `"External Req: {} {}"` | Log format | `LOG_REQUEST_FORMAT` |

---

### 4.8. HealthCheckAdapter.java
**Ruta:** `src/main/java/com/market/analysis/infrastructure/monitoring/HealthCheckAdapter.java`

| Línea | String | Tipo | Constante Sugerida |
|-------|--------|------|-------------------|
| 45 | `"Database health check failed"` | Error message | Migrar a `messages.properties` → `health.db_failed` |
| 64 | `"Database connection validation failed"` | Error message | Migrar a `messages.properties` → `health.db_validation_failed` |
| 68 | `"Failed to measure database connection time"` | Error message | Migrar a `messages.properties` → `health.db_measure_failed` |

---

### 4.9. SqlStrategyEvaluationRepository.java
**Ruta:** `src/main/java/com/market/analysis/infrastructure/persistence/repository/SqlStrategyEvaluationRepository.java`

| Línea | String | Tipo | Constante Sugerida |
|-------|--------|------|-------------------|
| 39 | `"Stock no encontrado con ID: "` | Error message | Migrar a `messages.properties` → `stock.not_found` |

---

## 5. CONSTANTES CRUZADAS (MÁXIMA PRIORIDAD)

Estos strings se usan en **múltiples archivos** y deben centralizarse inmediatamente:

### 5.1. IndicatorCode (Enum) - Usado en 5+ archivos
Todos los códigos de indicadores (`PRICE`, `SMA`, `EMA`, `RSI`, `MACD_LINE`, `MACD_SIGNAL`, `MACD_HIST`, `BB_UPPER`, `BB_LOWER`, `ATR`, `VOLUME`, `AVG_VOLUME`, `OPEN`, `HIGH`, `LOW`, `PREV_CLOSE`, `CONSTANT`, `VALUE`) están dispersos en:
- `RuleCapabilityCatalog.java` (definiciones canónicas)
- `RuleEvaluator.java` (etiquetas de formato)
- `FinvizFilterMapperImpl.java` (mapeo de filtros)
- `RiskRewardCalculator.java` (lookup SMA)
- `StrategyObjective.java` (validación SMA)

**Recomendación:** Crear enum `com.market.analysis.domain.model.IndicatorCode` (en singular)

### 5.2. EvaluationStatus (Enum) - Usado en 3 archivos
- `EvaluateStrategyService.java` (como constantes, líneas 30-31)
- `RuleEvaluator.java` (inline, línea 113)
- `AnalyzeAndPersistStockService.java` (inline, línea 83)

**Recomendación:** Crear enum `com.market.analysis.domain.model.EvaluationStatus`

### 5.3. HealthStatusCode (Enum) - Usado en 4 archivos
- `HealthCheckService.java` (líneas 74, 76, 87-89)
- `HealthCheckMapper.java` (línea 28)
- `HealthCheckController.java` (línea 47)

**Recomendación:** Crear enum `com.market.analysis.domain.model.HealthStatusCode`

### 5.4. DomainValidationException - Excepción de negocio para errores de validación
Los siguientes errores están duplicados en múltiples archivos del dominio y application:
- `"Strategy not found with id: "` (3 archivos)
- `"Strategy ID is required"` (2 archivos)
- `"Stock cannot be null"` (3 archivos)
- `"Strategy objective cannot be null"` (3 archivos)
- `"targetValue cannot be null"` (2 archivos)

**Recomendación:** Crear `DomainValidationException` con campo `errorCode` (String) y `params` (Object...). Cada archivo del dominio lanza la excepción con una clave única. El `GlobalExceptionHandler` en Presentation resuelve el mensaje via `MessageSource`.

### 5.5. "isEdit" (model attribute) - Usado en 2 archivos
- `StrategyController.java` (líneas 84, 97)
- `RuleDefinitionController.java` (líneas 43, 52)

**Recomendación:** Añadir `ATTR_IS_EDIT` a `WebConstants`

### 5.6. "SMA" (indicator code) - Usado en 3 archivos
- `RuleCapabilityCatalog.java` (líneas 40, 175, 181, 188)
- `RiskRewardCalculator.java` (líneas 175, 181, 188)
- `StrategyObjective.java` (línea 114)

**Recomendación:** Referenciar constante del Enum `IndicatorCode.SMA`

### 5.7. "Referer" HTTP header - Usado en 2 archivos
- `GlobalExceptionHandler.java` (línea 248)
- `BeanConfig.java` (línea 244, como `"HTTP-Referer"`)

**Recomendación:** Usar directamente la constante nativa de Spring: `org.springframework.http.HttpHeaders.REFERER` (no crear constante propia)

---

## 6. RESUMEN DE FICHEROS DE CONSTANTES

### 6.1. WebConstants.java (Presentation)
```java
package com.market.analysis.presentation.util;

public final class WebConstants {

    private WebConstants() {}

    // ── Model Attributes: RuleDefinition ──────────────────────────────
    public static final String ATTR_RULE_DEFINITIONS = "ruleDefinitions";
    public static final String ATTR_RULE_DEFINITION  = "ruleDefinition";
    public static final String ATTR_IS_EDIT         = "isEdit";
    public static final String ATTR_CAPABILITIES    = "capabilities";

    // ── Model Attributes: ProhibitedTicker ────────────────────────────
    public static final String ATTR_PROHIBITED_TICKERS   = "prohibitedTickers";
    public static final String ATTR_PROHIBITED_KEYWORDS  = "prohibitedKeywords";

    // ── Model Attributes: Strategy ────────────────────────────────────
    public static final String ATTR_STRATEGIES           = "strategies";
    public static final String ATTR_STRATEGY             = "strategy";
    public static final String ATTR_SUGGESTED_TICKERS    = "suggestedTickers";
    public static final String ATTR_DISCARDED_TICKERS    = "discardedTickers";
    public static final String ATTR_UNMAPPABLE_RULES     = "unmappableRules";
    public static final String ATTR_SUGGESTED_AT         = "suggestedAt";

    // ── Model Attributes: AnalyzeTicker ───────────────────────────────
    public static final String ATTR_TICKERS = "tickers";
    public static final String ATTR_TICKER  = "ticker";

    // ── Model Attributes: Error Handling ──────────────────────────────
    public static final String ATTR_ERROR_MESSAGE = "errorMessage";
    public static final String ATTR_ERROR_DETAILS = "errorDetails";
    public static final String ATTR_ERROR_TYPE    = "errorType";

    // ── Template Names: Rule Definitions ──────────────────────────────
    public static final String VIEW_RULE_DEFINITIONS_LIST   = "rule-definitions/list";
    public static final String VIEW_RULE_DEFINITIONS_CREATE = "rule-definitions/create";

    // ── Template Names: Prohibited Tickers ────────────────────────────
    public static final String VIEW_PROHIBITED_TICKERS_LIST = "prohibited-tickers/list";

    // ── Template Names: Strategies ────────────────────────────────────
    public static final String VIEW_STRATEGIES_LIST   = "strategies/list";
    public static final String VIEW_STRATEGIES_DETAIL = "strategies/detail";
    public static final String VIEW_STRATEGIES_CREATE = "strategies/create";

    // ── Template Names: Analysis ──────────────────────────────────────
    public static final String VIEW_ANALYSIS_LIST  = "analysis/analysis";
    public static final String VIEW_TICKER_DETAIL  = "analysis/ticker-detail";
    public static final String VIEW_TICKER_CHART   = "analysis/ticker-chart";

    // ── Template Names: Error ─────────────────────────────────────────
    public static final String VIEW_ERROR = "error";

    // ── Redirect URLs ─────────────────────────────────────────────────
    public static final String REDIRECT_RULE_DEFINITIONS      = "redirect:/rule-definitions";
    public static final String REDIRECT_PROHIBITED_TICKERS    = "redirect:/prohibited-tickers";
    public static final String REDIRECT_STRATEGIES            = "redirect:/strategies";
    public static final String REDIRECT_STRATEGIES_PREFIX     = "redirect:/strategies/";
    public static final String REDIRECT_ANALYSIS              = "redirect:/analysis";
    public static final String REDIRECT_ANALYSIS_TICKER_PREFIX = "redirect:/analysis/ticker/";

    // ── Paths ─────────────────────────────────────────────────────────
    public static final String PATH_HEALTH = "/health";

    // ── Default Values ────────────────────────────────────────────────
    public static final String DEFAULT_REFERER = "/";

    // ── UI Notification Types (Bootstrap) ─────────────────────────────
    public static final String TYPE_SUCCESS = "success";
    public static final String TYPE_DANGER  = "danger";
    public static final String TYPE_WARNING = "warning";
}
```

### 6.2. IndicatorCode.java (Domain - Enum)
```java
package com.market.analysis.domain.model;

import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public enum IndicatorCode {

    PRICE("PRICE"),
    SMA("SMA"),
    EMA("EMA"),
    RSI("RSI"),
    MACD_LINE("MACD_LINE"),
    MACD_SIGNAL("MACD_SIGNAL"),
    MACD_HIST("MACD_HIST"),
    BB_UPPER("BB_UPPER"),
    BB_LOWER("BB_LOWER"),
    ATR("ATR"),
    VOLUME("VOLUME"),
    AVG_VOLUME("AVG_VOLUME"),
    OPEN("OPEN"),
    HIGH("HIGH"),
    LOW("LOW"),
    PREV_CLOSE("PREV_CLOSE"),
    CONSTANT("CONSTANT"),
    VALUE("VALUE"),
    UNKNOWN("UNKNOWN");

    private final String code;

    private static final Map<String, IndicatorCode> BY_CODE =
            Arrays.stream(values())
                    .collect(Collectors.toMap(IndicatorCode::getCode, Function.identity()));

    IndicatorCode(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }

    public static IndicatorCode fromCode(String code) {
        IndicatorCode result = BY_CODE.get(code);
        if (result == null) {
            throw new IllegalArgumentException("Unknown indicator code: " + code);
        }
        return result;
    }
}
```

**Justificación de la optimización:**
- Iterar `.values()` en bucle tiene coste **O(n)** por cada lookup
- El mapa estático `BY_CODE` se cachea una sola vez al arrancar la clase (class loading)
- Las búsquedas posteriores tienen coste **O(1)** (hash map lookup)
- En un sistema que evalúa reglas técnicas sobre múltiples tickers, este método se invoca cientos/miles de veces por petición

### 6.3. HealthStatusCode.java (Domain - Enum)
```java
package com.market.analysis.domain.model;

public enum HealthStatusCode {

    UP("UP"),
    DOWN("DOWN"),
    DEGRADED("DEGRADED");

    private final String status;

    HealthStatusCode(String status) {
        this.status = status;
    }

    public String getStatus() {
        return status;
    }

    public static HealthStatusCode fromStatus(String status) {
        for (HealthStatusCode code : values()) {
            if (code.status.equals(status)) {
                return code;
            }
        }
        throw new IllegalArgumentException("Unknown health status: " + status);
    }
}
```

### 6.4. EvaluationStatus.java (Domain - Enum)
```java
package com.market.analysis.domain.model;

public enum EvaluationStatus {

    PASSED("PASSED"),
    FAILED("FAILED");

    private final String status;

    EvaluationStatus(String status) {
        this.status = status;
    }

    public String getStatus() {
        return status;
    }
}
```

### 6.5. DomainValidationException.java (Domain - Excepción de Negocio)
```java
package com.market.analysis.domain.exception;

/**
 * Excepción de negocio fuertemente tipada para errores de validación en el dominio.
 * Permite el desacoplamiento del dominio con mecanismos de renderizado/i18n.
 *
 * <p>El dominio lanza esta excepción con una {@code errorCode} (clave única)
 * y parámetros opcionales. El {@code GlobalExceptionHandler} de Presentation
 * resuelve el mensaje final usando {@code MessageSource}.</p>
 *
 * <p>Ejemplo de uso:</p>
 * <pre>
 *   throw new DomainValidationException("validation.target_price_null");
 *   throw new DomainValidationException("strategy.not_found", strategyId);
 * </pre>
 */
public class DomainValidationException extends RuntimeException {

    private final String errorCode;
    private final Object[] params;

    public DomainValidationException(String errorCode) {
        super(errorCode);
        this.errorCode = errorCode;
        this.params = new Object[0];
    }

    public DomainValidationException(String errorCode, Object... params) {
        super(errorCode);
        this.errorCode = errorCode;
        this.params = params;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public Object[] getParams() {
        return params;
    }
}
```

### 6.6. StrategyNotFoundException.java (Domain - Excepción de Negocio)
```java
package com.market.analysis.domain.exception;

/**
 * Excepción de negocio para cuando no se encuentra una estrategia por ID.
 * Permite al GlobalExceptionHandler resolver el mensaje via MessageSource.
 */
public class StrategyNotFoundException extends RuntimeException {

    private final String errorCode;
    private final Object[] params;

    public StrategyNotFoundException(String errorCode, Object... params) {
        super(errorCode);
        this.errorCode = errorCode;
        this.params = params;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public Object[] getParams() {
        return params;
    }
}
```

### 6.7. messages.properties (src/main/resources)
```properties
# ── Strategy ──────────────────────────────────────────────────────────
strategy.created=Estrategia creada correctamente.
strategy.updated=Estrategia actualizada correctamente.
strategy.deleted=Estrategia eliminada correctamente.
strategy.not_found=Strategy not found with id: {0}
strategy.empty_filters=No Finviz filters could be generated for this strategy.
strategy.finviz_degraded=Finviz no esta disponible temporalmente; la sugerencia se ha degradado sin resultados.
strategy.evaluation_failed=No se pudo generar una evaluacion determinista valida.
strategy.suggestion.unavailable=La sugerencia de tickers desde mercado no esta disponible todavia.
strategy.suggestion.failed=No se pudo sugerir tickers desde mercado en este momento.
strategy.suggestion.partial=Sugerencia parcial: revisa trazabilidad de descartes o reglas no mapeables.
strategy.suggestion.success=Sugerencias generadas correctamente desde mercado.
strategy.suggestion.none_added=No hay sugerencias aptas en snapshot para anadir.
strategy.tickers.switched=Ticker(s) cambiados a origen analisis: {0}.

# ── Rule Definition ───────────────────────────────────────────────────
ruledefinition.created=Definicion de regla creada correctamente.
ruledefinition.updated=Definicion de regla actualizada correctamente.
ruledefinition.deleted=Definicion de regla eliminada con exito.
ruledefinition.not_found=RuleDefinition not found with id: {0}

# ── Ticker ────────────────────────────────────────────────────────────
ticker.added=Ticker(s) anadidos y analizados correctamente.
ticker.updated=Datos del ticker actualizados correctamente.
ticker.deleted=Ticker ''{0}'' eliminado correctamente.
ticker.not_found=Ticker data not found for: {0}
ticker.ia.success=Valoracion IA generada y guardada correctamente.
ticker.ia.failed=No se pudo generar una valoracion IA valida. Se guardo un mensaje de fallback.
ticker.ia.fallback=No se pudo generar una valoracion interpretativa valida en este momento. Reintenta mas tarde.
ticker.apto=Ticker apto
ticker.no_apto=Ticker no apto

# ── Health Check ──────────────────────────────────────────────────────
health.app_up=Application is fully operational. All dependencies are healthy.
health.app_down=Application is not operational. Critical dependencies are unavailable.
health.app_degraded=Application is partially operational. Some dependencies have issues.
health.app_unknown=Unknown status
health.healthy=Healthy
health.unhealthy=Unhealthy
health.db_failed=Database health check failed
health.db_validation_failed=Database connection validation failed
health.db_measure_failed=Failed to measure database connection time

# ── Domain Validation (usado por DomainValidationException) ───────────
validation.strategy_objective_null=Strategy objective cannot be null
validation.stock_null=Stock cannot be null
validation.stock_data_null=Stock data cannot be null
validation.target_price_null=Target price cannot be null
validation.stop_price_null=Stop price cannot be null
validation.target_below_entry=Target price must be greater than entry price for long positions
validation.stop_above_entry=Stop price must be less than entry price for long positions
validation.risk_zero=Potential risk must be greater than zero
validation.capital_null=Capital to risk cannot be null
validation.risk_per_share_zero=Risk per share must be greater than zero
validation.sma_period_null=SMA period value cannot be null
validation.percentage_null=Percentage value cannot be null
validation.percentage_zero=Percentage value must be greater than zero
validation.strategy_null=Strategy cannot be null
validation.prompt_null=Prompt cannot be null
validation.request_null=Suggest tickers request cannot be null
validation.strategy_id_required=Strategy ID is required
validation.target_type_null=targetType cannot be null
validation.stop_loss_type_null=stopLossType cannot be null
validation.target_value_null=targetValue cannot be null
validation.stop_loss_value_null=stopLossValue cannot be null
validation.capital_to_risk_null=capitalToRisk cannot be null
validation.description_null=description cannot be null or blank
validation.target_value_zero=targetValue must be greater than zero
validation.stop_loss_value_zero=stopLossValue must be greater than zero
validation.capital_to_risk_zero=capitalToRisk must be greater than zero
validation.strategy_name_null=Strategy name cannot be null or empty
validation.strategy_desc_null=Strategy description cannot be null or empty
validation.strategy_no_rules=Strategy must contain at least one rule
validation.strategy_null_rule=Strategy cannot contain null rules
validation.rd_null=RuleDefinition cannot be null
validation.rd_code_null=RuleDefinition code cannot be null or empty
validation.rd_exists=RuleDefinition with code ''{0}'' already exists
validation.rd_id_null=RuleDefinition ID cannot be null for update
validation.rd_unsupported_code=Rule code ''{0}'' is not supported for this operation
validation.rd_param_conflict=Rule code ''{0}'' requires requiresParam={1}
validation.keyword_null=Prohibited keyword cannot be null
validation.keyword_exists=Prohibited keyword already exists: {0}
validation.keyword_blank=Keyword cannot be null or blank
validation.keyword_too_long=Keyword length must be <= {0}

# ── Infrastructure ────────────────────────────────────────────────────
finnhub.rate_limit=Limit exceeded when fetching quote for {0}
finnhub.no_data=No valid data found for: {0}
finnhub.api_error=API error for {0}
finnhub.unexpected=Unexpected error fetching quote {0}
polygon.comm_error=Error communicating with Polygon: {0}
polygon.unexpected=Unexpected error processing Polygon data for {0}
polygon.parse_error=Error parsing API response for ticker {0}
openrouter.error=Error calling OpenRouter API
config.finnhub_url_invalid=Finnhub base URL is not configured properly.
finviz.null_rule=Rule ''NULL_RULE'' cannot be mapped to Finviz filters.
finviz.rule_unmappable=Rule ''{0}'' cannot be mapped to Finviz filters.
stock.not_found=Stock no encontrado con ID: {0}
```

### 6.8. GlobalExceptionHandler - Ejemplo de Handler para DomainValidationException
```java
@ExceptionHandler(DomainValidationException.class)
public String handleDomainValidationException(
        DomainValidationException ex,
        RedirectAttributes ra,
        HttpServletRequest req,
        Locale locale) {

    String message = messageSource.getMessage(
            ex.getErrorCode(),
            ex.getParams(),
            locale);

    ra.addFlashAttribute(WebConstants.UI_NOTIFICATION_KEY,
            UiNotification.error(message));

    String referer = req.getHeader(HttpHeaders.REFERER);
    return "redirect:" + (referer != null ? referer : DEFAULT_REFERER);
}
```

### 6.9. ApiConstants.java (Infrastructure)
```java
package com.market.analysis.infrastructure.config;

public final class ApiConstants {

    private ApiConstants() {}

    // ── Finnhub ───────────────────────────────────────────────────────
    public static final String FINNHUB_ENDPOINT_QUOTE   = "/quote";
    public static final String FINNHUB_ENDPOINT_PROFILE = "/stock/profile2";

    // ── Polygon ───────────────────────────────────────────────────────
    public static final String POLYGON_URI_AGGREGATES = "v2/aggs/ticker/{ticker}/range/1/day/{from}/{to}";
    public static final String POLYGON_QUERY_ADJUSTED = "adjusted";
    public static final String POLYGON_QUERY_SORT     = "sort";
    public static final String POLYGON_SORT_DESC      = "desc";
    public static final String POLYGON_QUERY_LIMIT    = "limit";
    public static final String POLYGON_QUERY_API_KEY  = "apiKey";

    // ── Finviz ────────────────────────────────────────────────────────
    public static final String FINVIZ_SELECTOR_TABLE_ROWS   = "tbody tr";
    public static final String FINVIZ_SELECTOR_TICKER_LINK  = "a.tab-link, a[href*=ashx?t=]";

    // ── OpenRouter ────────────────────────────────────────────────────
    public static final String OPENROUTER_BASE_URL = "https://openrouter.ai/api/v1";
}
```

---

## 7. PRÓXIMOS PASOS (Plan Incremental)

### Paso 0: Inicializar i18n
Crear e inicializar el archivo `src/main/resources/messages.properties` con todas las claves de mensajes identificadas en este documento. Este paso es previo a cualquier refactorización y establece la base para la gestión centralizada de textos.

### Paso 1: Domain Validation Exceptions
1. Crear `DomainValidationException.java` en `domain/exception/`
2. Crear `StrategyNotFoundException.java` en `domain/exception/`
3. Crear `DuplicateEntityException.java` en `domain/exception/` (si aplica)
4. Refactorizar archivos de domain que usan strings de error para lanzar excepciones tipadas con claves
5. **Ejecutar tests de integración y verificar que pasan correctamente**
6. Commit del cambio

### Paso 2: Enums de Dominio (Domain)
1. Crear `IndicatorCode.java` como enum en `domain/model/`
2. Crear `HealthStatusCode.java` como enum en `domain/model/`
3. Crear `EvaluationStatus.java` como enum en `domain/model/`
4. Refactorizar todos los archivos de domain que usan esos strings para referenciar los enums
5. **Ejecutar tests de integración y verificar que pasan correctamente**
6. Commit del cambio

### Paso 3: WebConstants (Presentation)
1. Actualizar `WebConstants.java` con todas las constantes de la capa presentation
2. Refactorizar todos los controllers para usar las nuevas constantes
3. Eliminar constantes locales duplicadas
4. **Ejecutar tests de integración y verificar que pasan correctamente**
5. Commit del cambio

### Paso 4: GlobalExceptionHandler + i18n
1. Añadir handlers para `DomainValidationException` y `StrategyNotFoundException` en `GlobalExceptionHandler`
2. Inyectar `MessageSource` y resolver mensajes usando `errorCode` + `params`
3. Reemplazar `"Referer"` por `HttpHeaders.REFERER` de Spring
4. Migrar strings restantes de controllers/servicios a `messages.properties`
5. **Ejecutar tests de integración y verificar que pasan correctamente**
6. Commit del cambio

### Paso 5: Infrastructure Constants
1. Crear `ApiConstants.java` con headers, endpoints y query params
2. Refactorizar adaptadores para usar las nuevas constantes
3. **Ejecutar tests de integración y verificar que pasan correctamente**
4. Commit del cambio

### Paso 6: Validación Final
1. Ejecutar suite completa de tests
2. Verificar que no quedan strings hardcodeados en dominio (búsqueda global)
3. Verificar que dominio NO importa `MessageSource` ni `messages.properties`
4. Revisar cobertura de código
5. Actualizar documentación si es necesario

---

## 8. Cambios Recientes en el Codebase

### Commits relevantes desde la creación del plan:

| Commit | Descripción | Impacto en el Plan |
|--------|-------------|-------------------|
| `7cf0cc5` | Eliminada constante `SPANISH_LOCALE` no usada de `PromptBuilder` | ✅ Sin impacto - constante ya no existe |
| `82c04f8` | Ajustado `DEFAULT_MAX_CANDIDATES` en `SuggestTickersService` | ✅ Sin impacto - constante local ya extraída |
| `a1c2604` | Refactorizada estructura HTML en `list.html` | ✅ Sin impacto - solo templates |
| `90f65df` | Actualizadas properties de logging y SQL | ✅ Sin impacto - configuración |

### Archivos modified desde la creación del plan:

- `PromptBuilder.java`: Eliminada constante `SPANISH_LOCALE` no usada
- `SuggestTickersService.java`: Ajustado valor por defecto de `DEFAULT_MAX_CANDIDATES`
- `config/application.properties`: Cambios en configuración de logging
- `README.md`: Actualizaciones de documentación

**Nota:** Ninguno de estos cambios afecta la implementación del plan de extracción de constantes. Todos los archivos target del plan siguen en su estado original.

---

## 9. Notas Adicionales

- **Arquitectura Hexónica:** El dominio NUNCA debe importar `MessageSource` ni `messages.properties`. Los errores se comunican via `DomainValidationException` con claves tipadas.
- **Enum vs Constants:** Se priorizan Enums para `IndicatorCode`, `HealthStatusCode` y `EvaluationStatus` por su tipo fuerte, capacidad de iteración y seguridad en compilación.
- **i18n:** Todos los mensajes de usuario se centralizan en `messages.properties` para soporte multilingüe futuro. Solo la capa Presentation (GlobalExceptionHandler) resuelve los mensajes.
- **Spring Headers:** Se reutilizan constantes nativas del framework (`HttpHeaders.REFERER`) en lugar de crear duplicados.
- **Tests:** Se ejecutan tests de integración tras cada cambio mayor para detectar regresiones tempranas.
- **Commit incremental:** Cada paso se commitea por separado para facilitar rollback si es necesario.
- **Prioridad de implementación:** Seguir estrictamente el orden del Paso 0 al Paso 6 para evitar conflictos.
