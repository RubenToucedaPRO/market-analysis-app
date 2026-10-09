# Task 2026-10-09 — Mensaje amable de fallback IA en el div de análisis

## Título
Al fallar todos los modelos, la vista muestra el mensaje amistoso en el div
de análisis en vez de una tarjeta vacía; el log baja de ERROR a WARN.

## Resumen
- Rama `fix/ia-fallback-friendly-message` (desde `main` post-#186).
- Origen: logs de producción con los 5 modelos en 429/vacíos/inválidos. El
  sistema degradaba bien por dentro (fallback guardado, job DONE,
  `generated=false`), pero por fuera: tarjeta "Análisis técnico" vacía
  (el fragment solo renderizaba secciones estructuradas), flash genérico y
  `ERROR` + stacktrace en logs para una situación controlada.
- Cambios (5 ficheros):
  1. `ValorationSectionsDTO`: nuevo campo `raw` (texto guardado tal cual).
  2. `fragments/ai-valoration.html`: si no es estructurada, alerta warning
     con el texto crudo (el fallback amistoso ya guardado); `?:` con
     `#{ticker.ia.fallback}` como respaldo i18n. `th:text` (escapado).
  3. `ManageAnalyzeStockService`: el `catch` pasa de `log.error` con
     stacktrace a `log.warn` de una línea con tipo y mensaje (la caída total
     de modelos gratuitos es degradación prevista, no error).
  4. `messages.properties`: tildes en `ticker.ia.success/failed/fallback`
     (los tests ya esperaban el texto correcto; el fichero no).
  5. Nuevo `ValorationSectionsDTOTest` (2 tests: estructurada parte + raw;
     fallback expone raw). Primer intento falló por el mínimo de 40 chars
     por sección del validador; corregido alargando la conclusión.
- Sin cambios de lógica de negocio: el motor determinista y el flujo de
  fallback/validación quedan intactos; solo presentación + nivel de log.

## Código generado
Ver diff de la rama (DTO + fragment + service + properties + test nuevo).

## Decisiones técnicas tomadas
- Mostrar el texto crudo guardado (no forzar la key i18n): si algún día hay
  texto no estructurado real (datos antiguos), se ve en vez de una tarjeta
  vacía; para el fallback actual el texto YA es el mensaje amistoso.
- No tocar controller/job/polling: el flujo `?ia=failed` + flash sigue igual;
  el aviso en div es complementario, no sustituto.
- WARN de una línea conserva tipo+mensaje para diagnóstico sin el stacktrace
  (métricas `ai_valoration_metrics` siguen registrando el fallback).

## Cobertura de tests y pruebas añadidas
- `ValorationSectionsDTOTest` nuevo (2 tests).
- `mvn -B verify`: **1165/1165 verde**, JaCoCo OK.
- Re-análisis SonarQube: gate OK, 0 issues.
- Verificación runtime Docker (§3.7: `src/` + templates + properties):
  `docker compose down && docker compose up --build -d` + `GET /` → 200
  (ver abajo). Prueba visual del div: forzar fallo IA (sin red/tokens) y
  comprobar la alerta en el detalle del ticker.

## Advertencias de SonarQube o arquitectura
- Ninguna nueva (0 issues). Lógica fuera de vistas (solo presentación);
  `th:text` escapado; i18n sin hardcodeados.

## Próximos pasos sugeridos
1. Validar esta PR con menú (la tarea se detiene aquí según AGENTS.md §3.8).
2. Tras MERGED (13 Oct): **ADRs (4)** + **Prompt Library**.
