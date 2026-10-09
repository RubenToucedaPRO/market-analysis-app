# 08 — Degradación amistosa (friendly degradation)

## Cuándo
Integraciones externas falibles (LLMs con 429, APIs con cupo): el fallo total
debe ser un estado diseñado, no una excepción visible.

## Plantilla
> Todo fallo externo degrada a fallback persistido + métricas + mensaje
> amistoso i18n en la vista. El log baja a WARN de una línea (situación
> prevista, no error). La funcionalidad central nunca depende del servicio
> externo. Verifícalo provocando el fallo (key falsa, sin red en contenedor)
> y comprobando el mensaje visible.

## Ejemplo real
Caída total de los 5 modelos OpenRouter → fallback guardado, job DONE con
`generated=false`, alerta amarilla en el div de análisis ("No se pudo generar
una valoración… Reintenta más tarde"), sin stacktrace. PR #187. Doc:
`docs/task-2026-10-09-ia-fallback-friendly-message.md`.
