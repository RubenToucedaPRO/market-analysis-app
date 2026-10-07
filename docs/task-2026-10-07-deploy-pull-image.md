# Task 2026-10-07 — `docker compose pull` dentro de `deploy-tfm.sh`

- **Rama:** `fix/deploy-pull-image` (desde `main` = `e28b7f9`)
- **Alcance:** 2 ficheros en `deploy/` (+6/−3). Sin cambios en `src/`.
- **Fecha:** 2026-10-07.

---

## 1. Resumen de la tarea

`deploy-tfm.sh` hacía `git pull` del repo pero nunca `pull` de la imagen Docker: como `tfm/compose.yml` usa `image:` (pre-built en GHCR, sin sección `build:`), el `docker compose up -d --build app` reutilizaba en silencio la imagen local vieja. Resultado posible: el script decía "OK despliegue TFM" corriendo código antiguo (detectado al analizar el 504 de `suggest-tickers`: tras mergear el fix, el VPS seguiría con la imagen anterior).

## 2. Código generado

```bash
# deploy/tfm/deploy-tfm.sh — sección 2/2, antes del up:
# Sin este pull, 'up' reutilizaría la imagen local vieja y desplegaría código antiguo diciendo OK.
docker compose pull app || { echo "ERROR: no pude descargar la imagen de GHCR (¿red? ¿login? ¿existe el tag?). No sigo."; exit 1; }
```

- Cabecera del script: `build+up app` → `pull+up app desde GHCR` (el "build" mentía: ya no hay `build:` en el compose).
- `deploy/guia_vps.md` (FASE 14, bloque QUÉ es): misma corrección descriptiva + aviso explícito del porqué del `pull`.

## 3. Decisiones técnicas tomadas

1. **Fallo explícito, no fallback silencioso.** Si el `pull` falla (red, auth, tag inexistente), el script aborta con mensaje en llano en vez de seguir con la imagen vieja. Un deploy que no actualiza debe gritar, no susurrar "OK" (mismo principio que los guardianes `requireActive` del job service).
2. **`|| { ...; exit 1; }` además de `set -e`.** Estilo junior consistente con el resto del script (mensajes de error explicativos en cada guardia).
3. **Sin tocar el resto del flujo.** MySQL, volúmenes, `.env`, edge, certs y cron quedan igual; el `pull` solo afecta a la imagen `app`.

## 4. Cobertura de tests y pruebas

- Sin cambios en `src/` → `mvn test` no afectado (la suite estaba en 1112 verdes en `main`; este cambio no toca Java).
- `bash -n deploy/tfm/deploy-tfm.sh` → `SYNTAX OK`.
- Validación real: el próximo `./deploy-tfm.sh` en el VPS descargará la imagen nueva (verificable con `docker images` y el commit impreso por el script). Comportamiento ante fallo de red se deduce de `set -euo pipefail` + mensaje explícito.

## 5. Advertencias de SonarQube / arquitectura

- N/A (shell + docs; sin Java, sin secretos, sin cambios de arquitectura). El `pull` no expone credenciales (usa el auth Docker del host si hiciera falta).

## 6. Próximos pasos sugeridos

### A. Probar ahora (en el próximo deploy real)
1. `cd /opt/apps/tfm && ./deploy-tfm.sh` → ver la línea del `pull` descargando `:latest` nuevo.
2. Si GHCR falla: comprobar que el script aborta con el mensaje de error (no sigue con imagen vieja).

### B. Ideas futuras
1. Pinnear `TFM_TAG` a SHA en vez de `latest` para deploys reproducibles + rollback trivial (el workflow ya pushea ambos tags).
