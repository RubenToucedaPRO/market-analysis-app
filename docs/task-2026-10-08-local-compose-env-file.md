# Task 2026-10-08 — `env_file` en el compose local

- **Rama:** `fix/local-compose-env-file` (desde `main`)
- **Alcance:** `docker-compose.yml` + defaults OpenRouter en `application.properties` + este doc.
- **Tuning incluido** (commit del usuario en esta rama): `openrouter.fallback-models` → slugs vivos (`gemma-4-31b, laguna-xs-2.1, nemotron-3-ultra`; los anteriores daban 404 en free), `temperature` 0.7→0.2, `max-tokens` 500→1000.
- **Fecha:** 2026-10-08.
- **Excepción de flujo:** creada con PR #180 aún OPEN, a petición explícita del usuario.

---

## 1. Resumen de la tarea

En local, `OPENROUTER_FALLBACK_MODELS` (y `TEMPERATURE`, `MAX_TOKENS`, `TOP_P`, `FREQUENCY_PENALTY`, todas las `FINVIZ_*`) se ignoraban silenciosamente: el `docker-compose.yml` pasa variables con un bloque `environment:` explícito (lista blanca) y esas no estaban. El contenedor usaba los defaults del código aunque el `.env` tuviera otros valores. Se descartó que leyera `deploy/.env`: el compose local no tiene `env_file` y nada lo referencia.

## 2. Código generado

```yaml
    env_file:
      # Pasa el .env entero al contenedor para que ninguna propiedad nueva
      # quede fuera por olvido. Las claves de 'environment' tienen precedencia
      # y siguen mandando; si no existe .env se ignora y valen los defaults.
      - path: .env
        required: false
```

- Las líneas `environment:` existentes se mantienen intactas (precedencia + URL compuesta de datasource + arranque sin `.env`).
- Mismo mecanismo que prod (`tfm/compose.yml` ya usa `env_file`): paridad dev/prod.

## 3. Decisiones técnicas tomadas

1. **`env_file` en vez de añadir las 11 líneas que faltan** (decisión de usuario): el parche por líneas reintroduciría el bug con la próxima propiedad nueva; `env_file` lo elimina para siempre.
2. **`required: false`**: un clon fresco sin `.env` sigue arrancando con defaults (sin regresión de onboarding).
3. **Sin tocar `environment:`**: cero cambio de comportamiento hoy; la migración es aditiva.

## 4. Cobertura de tests y pruebas

- Sin cambios en `src/` → `mvn test` no afectado.
- Verificación: `docker compose config` válido y `OPENROUTER_FALLBACK_MODELS/TEMPERATURE/MAX_TOKENS` ya resuelven en el entorno del servicio `app` (antes ausentes). `FINVIZ_*` no aparece porque el `.env` local no las define (defaults mandan; fluirán solas cuando se añadan).
- Validación real: próximo `docker compose up -d` en localhost + `docker exec ... env | grep -i openrouter`.

## 5. Advertencias de SonarQube / arquitectura

- N/A (solo config Compose; sin Java, sin secretos nuevos — el `.env` ya lo leía Compose para interpolación).

## 6. Próximos pasos sugeridos

### A. Probar ahora (localhost del usuario)
1. `docker compose up -d` (recreate para que entre el `env_file`).
2. `docker exec market-analysis-app env | grep -i openrouter` → ver `FALLBACK_MODELS`, `TEMPERATURE=0.2`, `MAX_TOKENS=1000`.
3. Reintentar "Generar análisis".

### B. Ideas futuras
- Revisar slugs free vivos en `openrouter.ai/models` (rotan bajas: ya cayeron `meta-llama-3.3` y `gpt-oss-20b`).
