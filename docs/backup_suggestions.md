# Backup de sugerencias de mercado y plan de ejecución por fases

## 1) Análisis del estado actual (botón **"Sugerir tickers desde mercado"**)

### Flujo actual en la vista `strategies/detail`
1. Al pulsar **"Sugerir tickers desde mercado"** se ejecuta `POST /strategies/{id}/suggest-tickers`.
2. `StrategyController` invoca `SuggestTickersUseCase` y recibe `SuggestTickersResponseDTO`.
3. Se separan resultados en:
   - `suggestedTickers` (`APTO`)
   - `discardedTickers` (`NO_APTO`)
   - `unmappableRules`
   - `warnings` (solo para notificación)
4. Estos datos se guardan en **flash attributes** y se redirige a `GET /strategies/{id}`.
5. El fragmento `strategy-traceability.html` renderiza badges/listas con sugeridos, descartes y reglas no mapeables.

### Datos disponibles hoy
- A nivel respuesta de sugerencia:
  - `strategyId`
  - `appliedFilters`
  - `unmappableRules`
  - `warnings`
  - `suggestedTickers[]`
- Por ticker sugerido:
  - `ticker`
  - `suitabilityStatus`
  - `traceability[]`

### Limitaciones detectadas
- La trazabilidad de sugerencias es **efímera** (flash): se pierde al refrescar/navegar.
- No existe fecha/hora persistida de la ejecución de sugerencias.
- El botón **"Añadir sugeridos a análisis"** envía tickers a `/analysis/getTickerData`, que ejecuta `getStockData(...)` y vuelve a consultar proveedores externos para obtener/actualizar datos.

---

## 2) Objetivos funcionales solicitados

1. **Persistir** los datos necesarios de cada ticker sugerido y mostrarlos en `detail` hasta una nueva ejecución, incluyendo la **fecha de sugerencia**.
2. Si se pulsa **"Añadir tickers sugeridos"**, añadir esos tickers a analizados **sin recurrir a APIs externas**.

---

## 3) Plan de ejecución por fases

## Fase 1 — Persistencia del snapshot de sugerencias
**Objetivo:** dejar de depender de flash attributes para la trazabilidad.

### Alcance
- Crear un modelo persistente de ejecución de sugerencias por estrategia (snapshot):
  - `strategyId`
  - `suggestedAt` (fecha/hora ejecución)
  - `appliedFilters`
  - `warnings`
  - colección de resultados por ticker (`ticker`, `status`, `traceability`).
- Guardar snapshot al terminar `suggestTickers`.
- En `GET /strategies/{id}`, cargar el último snapshot y enviarlo al modelo para render.

### Resultado esperado
- La vista `detail` mantiene los datos sugeridos/descartados y reglas no mapeables tras recarga.
- Se muestra explícitamente la fecha de última sugerencia.

## Fase 2 — Contrato de “datos necesarios” para alta sin APIs
**Objetivo:** definir qué mínimos datos deben guardarse para crear entradas analizadas offline.

### Alcance
- Definir DTO/objeto de “snapshot sugerido utilizable para alta” con campos mínimos para `StockData` y evaluación inicial:
  - `ticker`
  - `strategyId`
  - métricas deterministas ya calculadas en sugerencia (si existen)
  - trazabilidad y estado apto/no apto
  - timestamp de sugerencia.
- Ajustar el flujo de sugerencia para persistir estos campos por ticker apto.

### Resultado esperado
- Cada ticker apto queda preparado para añadirse a análisis sin depender de una consulta inmediata externa.

## Fase 3 — Nuevo caso de uso de alta desde snapshot
**Objetivo:** desacoplar la acción “añadir sugeridos” del flujo `getStockData(...)`.

### Alcance
- Implementar un caso de uso específico (p. ej. `AddSuggestedTickersToAnalysisUseCase`):
  - Entrada: `strategyId` + identificador del snapshot (o “último snapshot”).
  - Acción: crear registros de tickers analizados desde datos persistidos del snapshot.
  - Sin llamadas a `StockProviderPort` ni `HistoricalProviderPort`.
- Actualizar botón de UI para apuntar al nuevo endpoint/caso de uso.

### Resultado esperado
- Pulsar **"Añadir tickers sugeridos"** agrega tickers a análisis usando datos ya guardados.

## Fase 4 — Reconciliación y refresco opcional posterior
**Objetivo:** mantener coherencia temporal sin bloquear el alta offline.

### Alcance
- Marcar registros creados desde snapshot como `origin=SUGGESTION_SNAPSHOT`.
- Permitir refresco manual o batch posterior para enriquecer datos externos (opcional, no bloqueante).

### Resultado esperado
- Alta instantánea sin APIs externas y posibilidad de actualización controlada después.

## Fase 5 — Pruebas y criterios de aceptación
**Objetivo:** asegurar comportamiento estable y trazable.

### Pruebas clave
- Persistencia y lectura del último snapshot por estrategia.
- Render en `detail` con fecha de sugerencia.
- “Añadir sugeridos” sin invocar puertos externos (tests de interacción/mock).
- Regresión: el flujo actual de sugerencia sigue clasificando APTO/NO_APTO y reglas no mapeables.

### Criterios de aceptación
- La vista conserva sugerencias y fecha hasta nueva ejecución.
- El alta de sugeridos funciona sin consumo de APIs externas.
- No se mueve lógica de negocio a Thymeleaf; orquestación en Application y control en Presentation.
