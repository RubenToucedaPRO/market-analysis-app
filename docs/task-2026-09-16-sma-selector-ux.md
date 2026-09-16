# Mejora UX del selector SMA en gestión de riesgo

- Fecha: 2026-09-16
- Slug: `sma-selector-ux`
- Alcance: formulario `strategies/create` (create/edit) + `strategies/detail`

## Resumen

Al elegir tipo `SMA` en objetivo o stop-loss, el campo "Valor Objetivo" mostraba un
desplegable con 3 números pelados (`20 / 50 / 200`) sin contexto. Esos 3 valores son
correctos (únicos periodos SMA que el dominio calcula), pero la UX era confusa:
misma etiqueta para % / precio / periodo, sin explicar que SMA es un precio dinámico.

Se ha mejorado solo presentación (sin tocar dominio):

- Opciones etiquetadas: `SMA 20 - Corto plazo`, `SMA 50 - Medio plazo`, `SMA 200 - Largo plazo`.
- Etiqueta del campo cambia según tipo: `Valor objetivo (%)` / `Precio objetivo ($)` / `Periodo SMA`
  (análogo para stop-loss).
- Icono tooltip (Bootstrap) junto a la etiqueta, visible solo en modo SMA, explicando que
  el valor es dinámico y los periodos disponibles.
- `detail.html` muestra `SMA 50 (Medio plazo)` en vez del número pelado.

## Código generado

### 1. `templates/fragments/risk-management-fields.html`

- Columnas `col-md-4/2` → `col-md-3/3` para que quepan las etiquetas largas.
- `select#objectiveTargetType` / `#objectiveStopLossType`: nuevos `th:data-label-*` y
  `th:data-placeholder-*` (i18n, sin strings hardcodeados en JS).
- Labels con `id` (`objectiveTargetValueLabel`, `objectiveStopLossValueLabel`) + icono
  `<i id="...SmaHelp" class="bi bi-info-circle ... d-none" data-bs-toggle="tooltip"
  th:title="#{risk.sma.help}">`.
- Selects SMA con `th:data-default-option`, `th:data-short/medium/long` y opción
  por defecto `th:text="#{risk.sma.period}"`.

### 2. `static/js/risk-management.js`

- Nuevo `getSmaHorizonLabel(period, selectEl)` (20/50/200 → data-short/medium/long) y
  `formatSmaOptionLabel()` → `"SMA 20 - Corto plazo"`, fallback `"SMA {p}"` para
  periodos futuros del catálogo.
- `populateSmaPeriodSelect()` usa `dataset.defaultOption` en vez del literal hardcodeado.
- Nuevo `updateValueFieldHints()` actualiza label, placeholder y visibilidad del tooltip.
- `handleObjectiveTypeChange()` amplía firma con `labelId, helpId`; init y listeners
  actualizados para target y stop. `syncSelectToInput()` sin cambios (binding intacto).

### 3. `messages.properties` (nuevas claves)

```properties
risk.target_value.percentage=Valor objetivo (%)
risk.target_value.fixed=Precio objetivo ($)
risk.target_value.sma=Periodo SMA
risk.stop_value.percentage=Valor stop loss (%)
risk.stop_value.fixed=Precio stop loss ($)
risk.stop_value.sma=Periodo SMA
risk.sma.period=-- Periodo SMA --
risk.sma.help=Con SMA el objetivo/stop es dinámico: usa el valor actual de la media, no un precio fijo. Solo periodos 20 (corto), 50 (medio) y 200 (largo).
risk.sma.short=Corto plazo
risk.sma.medium=Medio plazo
risk.sma.long=Largo plazo
```

### 4. `templates/strategies/detail.html`

- Bloques `th:if="... != 'SMA'"` (número con 2 decimales, como antes) y
  `th:if="... == 'SMA'"` (`SMA {v}` + horizonte entre paréntesis vía `risk.sma.*`).
- SpEL cortas, sin lógica de negocio; solo presentación.

### 5. `templates/strategies/create.html`

- Añadido `<script th:src="@{/js/tooltips-init.js}">` (los tooltips SMA lo requieren;
  `fragments/scripts` no lo incluía).

## Decisiones técnicas

- **Sin cambios de dominio**: `RuleCapabilityCatalog (20,50,200)`, `StrategyObjective.validateSmaPeriod`
  y `RiskRewardCalculator` intactos. Permitir SMA arbitraria exigiría persistencia, cálculo
  histórico, gráficos y Finviz; fuera de alcance.
- **Sin hardcodear en JS/vista**: periodos siguen viniendo de `globalThis.ruleDefinitions`;
  solo el etiquetado de horizonte (20→corto, 50→medio, 200→largo) es un mapa en JS con
  textos vía `data-*` i18n y fallback neutro.
- **Tooltip frente a help-text**: elegido tooltip por petición; menos intrusivo. El icono
  se inicializa con `tooltips-init.js` en el load (el `d-none` no rompe Bootstrap Tooltip).
- **SRP**: `risk-management.js` sigue separado de `strategy-manager.js`.
- **Thymeleaf**: `th:text` en todo, `th:title`/`th:aria-label` para el tooltip, CSRF intacto.

## Cobertura de tests

- Nuevos en `StrategyControllerViewTest`:
  - `shouldRenderSmaSelectorsWithI18nHintsInCreateForm`: `GET /strategies/new` contiene
    `objectiveTargetSmaSelect`, `objectiveStopLossSmaSelect`, `...ValueLabel`, `...SmaHelp`,
    `data-label-sma` y `Periodo SMA`.
  - `shouldRenderSmaTargetWithHorizonInDetail`: objetivo SMA 50 / stop SMA 20 renderizan
    `SMA 50` y `SMA 20` en `GET /strategies/1`.
- Ejecución:
  - `mvn test -Dtest=StrategyControllerViewTest` → `Tests run: 9, Failures: 0, Errors: 0`.
  - `mvn test -Dtest='StrategyObjectiveTest,RiskRewardCalculatorTest,ManageStrategyServiceP0Test,ManageStrategyServiceTest'`
    → `Tests run: 89, Failures: 0, Errors: 0`.
  - Suite completa tras fusionar `main`: `Tests run: 1027, Failures: 0, Errors: 0, BUILD SUCCESS`.
- Sin `lenient` Mockito; sin cambios de dominio que exijan tests nuevos de dominio.

## Advertencias SonarQube / arquitectura

- SpEL en detail: expresiones cortas con un solo operador; horizonte resuelto con `th:if`
  por periodo en vez de ternarios anidados.
- JS: funciones pequeñas, complejidad cognitiva baja; sin `System.out`, sin recursos manuales.
- Constructor injection / capas: no aplica (solo vista + JS estático + properties).
- Revisar en Sonar: duplicación de los dos bloques SMA en `detail.html` (target/stop) —
  aceptable por simetría con el resto de la vista; extraer a `th:fragment` si Sonar lo marca.

## Próximos pasos sugeridos

### A. Probar ahora (login con `APP_SECURITY_*`, en `http://localhost:8080`)

1. Ve a `/strategies/new`, en "Tipo de Objetivo" elige `SMA` → la etiqueta
  cambia a "Periodo SMA", el desplegable ofrece "SMA 20 - Corto plazo",
  "SMA 50 - Medio plazo" y "SMA 200 - Largo plazo", y aparece un icono ⓘ
  junto a la etiqueta. Pasa el ratón por el icono → ves el texto de ayuda
  ("...usa el valor actual de la media..."). Lo mismo vale para el Stop Loss.
2. Ve a `/strategies/50` (estrategia "Precio superior a SMA20") → en Objetivo
  y Stop Loss ves `SMA 50 (Medio plazo)` en vez de un número pelado.
  Resultado visible DISTINTO del de estrategias con objetivo en % o precio
  (que muestran el número con 2 decimales, como antes).

### B. Ideas futuras (opcionales, NO hacer ahora)

1. Si se amplía el catálogo SMA en el futuro, el select lo hereda solo; añadir el horizonte
   correspondiente en `getSmaHorizonLabel` + `messages.properties`.
2. Valorar mismo patrón de etiqueta dinámica en reglas (`strategy-manager.js`) si gusta el resultado.
