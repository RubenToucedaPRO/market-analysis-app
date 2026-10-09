# 02 — Investigar antes de cambiar (investigate before change)

## Cuándo
Antes de cualquier fix: bugs, avisos SonarQube/SonarLint, refactors.

## Plantilla
> No cambies nada todavía. Lee los ficheros implicados (código + tests +
> docs), reproduce o localiza la causa con evidencia (logs, queries, blame)
> y explícame: qué pasa, por qué pasa y opciones de arreglo con coste.
> Solo implementamos tras mi validación.

## Ejemplo real
Aviso S8688 en `PolygonAdapter`: se verificó en código que era el
`LocalDate.now()` sin zona, se explicó el riesgo real (~cero en ventana de
350 días) y el usuario aplicó `ZoneId America/New_York` (mejor que UTC para
dato americano). Verificación posterior por lectura, sin tocar nada.
