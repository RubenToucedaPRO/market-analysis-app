# Task 2026-10-07 — Endurecimiento y parametrización del despliegue VPS (`deploy/`)

- **Rama:** `feat/deploy-hardening` (6 commits sobre `main` = `429dd1d`)
- **PR:** #176 (abierta, `Feat/deploy hardening`, base `main`)
- **Alcance:** 16 ficheros nuevos en `deploy/`, +3104 líneas, 0 borrados. Sin cambios en `src/`, `pom.xml` ni workflows.
- **Fecha verificación en vivo:** 2026-10-02 (VPS `141.94.250.163`, redeploy completo FASE 11 → 15a) y 2026-10-07 (checks `nmap`/`ss`/`free`/`docker stats`).

---

## 1. Resumen de la tarea

Publicar y endurecer la infraestructura de despliegue del TFM en VPS (OVH, Ubuntu 24.04):

1. **Limpieza de la guía** (`deploy/guia_vps.md`): eliminar contenido ajeno al TFM (restos de la guía de otro proyecto). La guía queda solo-TFM.
2. **Parametrización**: extraer dominio, email, host, repositorio e imagen a variables del `.env` (`TFM_DOMAIN`, `CERTBOT_EMAIL`, `VPS_HOST`, `GHCR_REPOSITORY`, `TFM_TAG`) y renderizar los configs de Nginx con `envsubst` (paquete `gettext`).
3. **Corrección de bugs reales** encontrados durante el redeploy de validación:
   - `render-nginx-config.sh` no exportaba `TFM_DOMAIN` → `envsubst` lo sustituía por vacío (`server_name ;`, Nginx en bucle `Restarting`).
   - Faltaban los `scp` de scripts y `tfm-http-only.conf` en FASE 13.
   - Duplicación de `mkdir` entre FASE 11 y FASE 13.
   - `GHCR_REPOSITORY` con mayúsculas → Docker rechaza la referencia (`must be lowercase`).
   - MariaDB oficial exige `MARIADB_*`/`MYSQL_*`, no `DB_*` (documentado; el `.env` del VPS los define).
4. **Backups**: verificación de integridad (`gzip -t` + SHA256) en `backup-mariadb.sh`, variables `VPS_HOST`, procedimiento de **restore test** (sección 38) y **copia externa** manual + cloud/`rclone` (sección 37).
5. **Seguridad verificada**: ningún secreto real en el repo (`deploy/.env` no trackeado; scan del diff limpio).

La validación no fue teórica: se hizo un **redeploy limpio completo en el VPS real** siguiendo la guía (FASE 11 → 15a) y se comprobó cada fase.

---

## 2. Código generado (ficheros en `deploy/`)

| Fichero | Líneas | Propósito |
|---|---|---|
| `guia_vps.md` | ~2580 | Guía junior paso a paso solo-TFM (FASE 1 → 20, 15a, 37, 38) |
| `.env.tfm.example` | 38 | Plantilla con 14 vars (BD, login, APIs, dominio, despliegue) |
| `tfm/compose.yml` | 90 | Stack app+MySQL (sin `ports:`, `image:` desde GHCR) |
| `tfm/deploy-tfm.sh` | 76 | Wrapper único de deploy: `git pull` → `up mysql` → `build+up app` → checks → render Nginx |
| `tfm/backup-mariadb.sh` | 42 | Backup con retención 7 días + `gzip -t` + SHA256 |
| `edge/compose.yml` | 33 | Nginx, único servicio con `ports: 80/443` |
| `edge/nginx.conf` | 22 | Base Nginx (los `server` van en `conf.d/`) |
| `edge/conf.d/tfm.conf` | 41 | Server HTTPS con placeholders `${TFM_DOMAIN}` |
| `edge/conf.d/tfm-http-only.conf` | 17 | Server solo-HTTP para el reto ACME (paso A) |
| `scripts/render-nginx-config.sh` | 18 | Render in-place `${TFM_DOMAIN}` vía `envsubst` |
| `scripts/certbot-first-cert.sh` | 29 | Primer cert, lee `TFM_DOMAIN`/`CERTBOT_EMAIL` del `.env` |
| `scripts/renew-hook-reload-nginx.sh` | 7 | Deploy-hook Certbot → `reload` de Nginx Docker |
| `scripts/fail2ban-install-nginx-401.sh` | 13 | Instala la jail `nginx-401` en el host |
| `fail2ban/filter.d/nginx-401.conf` | 10 | Filtro 401/403 (excluye reto ACME) |
| `fail2ban/jail.d/nginx-401.local` | 13 | Celda: 10 fallos/5min → ban 1h en 80/443 |
| `ssh-config.example` | 8 | Alias `vps-tfm` de ejemplo |

Snippets clave:

```bash
# scripts/render-nginx-config.sh (con el fix export)
set -euo pipefail
if [[ -f /opt/apps/tfm/.env ]]; then
  # shellcheck disable=SC1091
  source /opt/apps/tfm/.env
fi
: "${TFM_DOMAIN:?TFM_DOMAIN no definido. ...}"
# envsubst solo ve variables exportadas, no variables de shell sin exportar.
export TFM_DOMAIN
for f in /opt/apps/edge/conf.d/tfm.conf /opt/apps/edge/conf.d/tfm-http-only.conf; do
  envsubst '${TFM_DOMAIN}' < "$f" > "$f.tmp" && mv "$f.tmp" "$f"
done
```

```yaml
# tfm/compose.yml (imagen pre-built, vars del .env)
image: ghcr.io/${GHCR_REPOSITORY:-ruben/market-analysis-app}/tfm:${TFM_TAG:-latest}
```

```bash
# tfm/backup-mariadb.sh (verificación de integridad)
gzip -t "$OUT" || { echo "ERROR: backup corrupto"; exit 1; }
CHECKSUM="$(sha256sum "$OUT" | cut -d' ' -f1)"
```

```env
# .env.tfm.example (extracto nuevas vars)
TFM_DOMAIN=tfm.tudominio.es
CERTBOT_EMAIL=tu@email.com
VPS_HOST=141.94.250.163
GHCR_REPOSITORY=ruben/market-analysis-app
TFM_TAG=latest
```

---

## 3. Decisiones técnicas tomadas

1. **`.env` como única fuente de verdad** (`TFM_DOMAIN`, `CERTBOT_EMAIL`, `VPS_HOST`, `GHCR_REPOSITORY`, `TFM_TAG`). Un solo sitio para cambiar dominio/imagen/host.
2. **`envsubst` (gettext) en vez de `sed`.** Sustitución exacta por nombre de variable, sin riesgo de reemplazos parciales. Requiere instalar `gettext` en el VPS (documentado).
3. **Sin renombrar ficheros a `.template`** (petición explícita del usuario). Los `.conf` llevan el placeholder `${TFM_DOMAIN}` y se renderizan in-place. Contrapartida asumida: el render es destructivo (pierde el placeholder); si un render falla con dominio vacío hay que re-copiar las plantillas pristine antes de reintentar (documentado en el flujo A/B/C).
4. **Imagen pre-built desde GHCR, no `build:` en el VPS.** El build lo hace GitHub Actions (workflow `docker.yml`, ya en `main` vía PR #175); el VPS solo hace `pull`+`up`. Ahorra RAM/CPU en VPS pequeño y fija versiones (`mariadb:10.11`, tag de imagen).
5. **Se mantiene el clon `./repo` en el VPS** (decisión explícita del usuario tras valorar alternativas). Motivos: provee `script-bd.sql` al init de MariaDB vía volumen y da trazabilidad (`git log` = commit desplegado). Alternativas aparcadas: copiar solo el script (pierde trazabilidad) y Flyway (salto profesional futuro).
6. **Sin secretos en git.** `deploy/.env` (con passwords y tokens reales) existe solo local/VPS y no está trackeado. Solo se publica `.env.tfm.example` con placeholders. Scan del diff de la PR: limpio.
7. **Validación en vivo, no solo lectura.** Cada cambio se probó en el VPS real durante el redeploy (incluidos los 3 bugs de render/scp/mkdir, reproducidos y corregidos allí).

---

## 4. Cobertura de tests y pruebas

- **Sin cambios en `src/`** → `mvn test` ejecutado como sanity: **745 run, 0 failures, 0 errors, 0 skipped**. No hay tests que cubran `deploy/` (docs + shell).
- **Scripts shell:** `bash -n` OK (`render-nginx-config.sh`); resto validados por ejecución real en VPS.
- **Validación end-to-end en VPS real (2026-10-02, FASE 11 → 15a):**
  - `deploy-tfm.sh`: `git pull` → `mysql healthy` → `Started Application` → checks `8080/5005` no públicos → render Nginx con dominio real (verificado con `grep server_name`).
  - Edge A/B/C: HTTP 200 `edge HTTP OK` → certs `fullchain.pem`+`privkey.pem` → HTTPS 200.
  - `nmap`: solo 22/80/443 abiertos; resto `filtered` (Edge OVH en drop).
  - `ss -tulpn`: solo `docker-proxy` :80/:443, `sshd` :22; nada en :8080/:3306/:5005 (Regla de Oro OK).
  - `fail2ban-client status nginx-401`: jail activa sobre `access.log`, 0 baneadas.
  - `backup-mariadb.sh`: genera `.sql.gz` > 0 + `gzip -t` + SHA256; cron `30 2 * * *` operativo.
  - Restore test (FASE 38): OK contra `mariadb-test` temporal, conteos cuadran con prod.
  - `free -h`: `available` 2.7 GiB; swap 2G activo sin uso; Docker total ~590 MB.
  - GitHub Actions `docker.yml` (PR #175, en `main`): run verde, push `:latest` + `:<sha>` a GHCR verificado en logs.
- **Cubierto por validación manual en VPS, no requiere prueba adicional en navegador** salvo la checklist web de abajo.

---

## 5. Advertencias de SonarQube / arquitectura

- **SonarQube Java: N/A** (la PR no toca `src/`, `pom.xml` ni vistas Thymeleaf).
- **Shell scripts:** todos con `set -euo pipefail`; sin `curl | bash`; sin credenciales hardcodeadas (leen `.env` con `chmod 600`); `backup-mariadb.sh` usa `set -a` para exportar solo lo necesario. Pendiente ideal: pasar `shellcheck` en CI futuro (no bloqueante).
- **Arquitectura:** cambios solo en capa Infrastructure-as-docs (`deploy/`); dominio/application intactos. Separación edge (puerta) / tfm (app+datos) preservada; un solo servicio con `ports:`.
- **Seguridad:** Dominios e IPs fuera del código; secretos solo en `.env` (VPS + Bitwarden, nunca git); jail `nginx-401` excluye `/.well-known/acme-challenge/` para no autobanear a Let's Encrypt.
- **Riesgo conocido y asumido:** render in-place destructivo (ver decisión 4). Mitigado con procedimiento de re-copia de plantillas en la guía.

---

## 6. Próximos pasos sugeridos

### A. Probar ahora (tras el merge, en prod)
1. `https://tfm.rubentouceda.es` → 200, login con usuario prod → dashboard OK.
2. Crear una estrategia de prueba → evaluar → ver métricas (confirma BD escribible tras el redeploy).
3. Forzar 1 backup manual (`/opt/apps/tfm/backup-mariadb.sh`) → `ls -l /opt/backups` con fecha de hoy.
4. `sudo fail2ban-client status nginx-401` → jail presente.

### B. Ideas futuras (PRs separadas, no en esta)
1. **Nivel 1 — 504 en `suggest-tickers`:** añadir `proxy_read_timeout 420s` (+connect/send) a `tfm.conf` (20 llamadas Polygon a 5/min = ≥240s + LLM). Fix infra rápido.
2. **Nivel 2 — B1 async+polling** para `suggest-tickers`: `202` + `jobId`, worker `@Async`, `GET /suggest-jobs/{id}`, polling JS. Elimina el problema de timeouts de raíz.
3. **Copia externa automática** con `rclone` + `crypt` a B2 (documentada en FASE 37, pendiente implementar y probar el restore desde cloud).
4. **Flyway** para migraciones versionadas (entonces sí se podría eliminar el clon `./repo` del VPS).
5. **`shellcheck`** en CI para `deploy/scripts/*.sh` y `deploy/tfm/*.sh`.
6. **GitHub Actions de deploy** (FASE 18 de la guía) una vez estabilizado el flujo manual.
