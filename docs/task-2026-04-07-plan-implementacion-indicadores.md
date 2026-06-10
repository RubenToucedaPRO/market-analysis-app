# Plan de Implementación de Indicadores Técnicos — Documento de Tarea

**Fecha:** 2026-04-07  
**Tarea:** Elaborar plan de implementación por fases para EMA, MACD, RSI, Bollinger Bands y ATR, solicitado en el PR de análisis de indicadores técnicos.

---

## Resumen de la Tarea

Se ha creado el archivo `implementacion_reglas.md` en la raíz del repositorio con un plan de implementación en 6 fases independientes para añadir los indicadores técnicos seleccionados al motor de reglas existente.

---

## Fichero generado

`/implementacion_reglas.md`

---

## Decisiones Técnicas

1. **6 fases independientes**: Cada fase compila y sus tests pasan por separado, permitiendo revisión incremental.
2. **Fase 1 como base**: Todos los campos del modelo de dominio y persistencia se declaran en la primera fase para evitar referencias pendientes en fases posteriores.
3. **EMA antes que MACD**: MACD depende de EMA(12), EMA(26) y EMA(9); por eso EMA es la Fase 2 y MACD la Fase 4.
4. **Bollinger Bands y ATR en la misma fase (5)**: BB reutiliza la SMA existente + cálculo de desviación estándar; ATR reutiliza las velas OHLCV ya disponibles. Ninguna depende de EMA/MACD, por lo que pueden implementarse en paralelo.
5. **Tests en la última fase**: Siguiendo el patrón del proyecto (tests reflejan comportamiento ya implementado), la Fase 6 cubre con tests todos los métodos nuevos de `StockHistoricalService` y todos los nuevos cases de `RuleEvaluator`.
6. **Sin cambio de esquema de BD para `rule_definition`**: La tabla ya soporta indicadores parametrizados con `requires_param=true`; solo se necesita insertar los nuevos registros como datos.

---

## Próximos Pasos

Indicar a Copilot qué fase implementar. Orden sugerido:
1. Implementar Fase 1 (modelo y persistencia)
2. Revisar y validar
3. Implementar Fase 2 (EMA)
4. Revisar y validar
5. Continuar con Fases 3, 4, 5 y 6
