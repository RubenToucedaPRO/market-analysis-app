#!/usr/bin/env bash
# Despliegue del TFM en el VPS (junior: única vía soportada para levantar la app).
# Uso: cd /opt/apps/tfm && ./deploy-tfm.sh
# Hace: git pull del repo → up mysql → build+up app → checks.
# NUNCA ejecutar docker compose dentro de ./repo (crearía otra BD duplicada
# y publicaría 8080/5005 al mundo). Si lo intentas aquí por error, me niego.
set -euo pipefail

# --- Guardia anti-despiste (Capa 1): ¿estoy dentro del repo clonado? ---
if [[ -f ./pom.xml ]] || grep -qE '^\s*build:\s*\.\s*$' ./compose.yml 2>/dev/null; then
  echo "ERROR: estás dentro del repo clonado (./repo) o en un compose de desarrollo."
  echo "Aquí NO se hace 'docker compose up': crearías redes/volúmenes duplicados"
  echo "(repo_*) y publicarías 8080/5005 al mundo (UFW no lo evitaría)."
  echo "Ve a /opt/apps/tfm y ejecuta ./deploy-tfm.sh"
  exit 1
fi

cd /opt/apps/tfm

# --- Requisitos previos (mensajes en llano) ---
[[ -f ./.env ]] || { echo "ERROR: falta /opt/apps/tfm/.env (créalo desde .env.tfm.example, chmod 600)."; exit 1; }
[[ -d ./repo ]] || { echo "ERROR: falta /opt/apps/tfm/repo. Clónalo: git clone <tu-repo> /opt/apps/tfm/repo"; exit 1; }
docker network inspect front >/dev/null 2>&1 || { echo "ERROR: falta la red 'front'. Crea: docker network create front"; exit 1; }

# --- Código más reciente + versión visible (rollback = este commit) ---
git -C ./repo pull --ff-only
echo "Commit desplegado: $(git -C ./repo rev-parse --short HEAD) ($(git -C ./repo log -1 --format=%s))"

# --- 1/2 BD primero (no necesita tu código) ---
docker compose up -d mysql
echo "Esperando mysql healthy..."
for _ in $(seq 1 30); do
  STATUS="$(docker inspect -f '{{.State.Health.Status}}' market-analysis-mysql 2>/dev/null || echo "?")"
  [[ "$STATUS" == "healthy" ]] && break
  sleep 5
done
[[ "$STATUS" == "healthy" ]] || { echo "ERROR: mysql no está healthy (ver: docker compose logs mysql). No sigo con la app."; exit 1; }

# --- 2/2 App (build del repo + arranque) ---
docker compose up -d --build app
echo "Esperando Started Application (hasta ~5 min en VPS pequeño)..."
# Bucle con logs --tail (sin -f): evita el falso ERROR por SIGPIPE+pipefail
# que daba 'logs -f | grep -qm1' al cerrar grep tras el match.
FOUND=0
DEADLINE=$((SECONDS+300))
while (( SECONDS < DEADLINE )); do
  if docker compose logs --tail=200 app 2>&1 | grep -qm1 -E "Started .*Application|StartedApplication|Application started"; then FOUND=1; break; fi
  sleep 10
done
if [[ "$FOUND" == "1" ]]; then
  echo "App arrancada."
else
  echo "ERROR: la app no mostró arranque. Mira: docker compose logs --tail=60 app"
  echo "Si es 'validate'/tablas: revisa logs de mysql ANTES de tocar volúmenes."
  exit 1
fi

# --- Checks: nada público salvo edge ---
docker compose ps
if sudo ss -tulpn 2>/dev/null | grep -qE '0\.0\.0\.0:(8080|5005)'; then
  echo "ERROR: veo 0.0.0.0:8080 o :5005 públicos. ¿Levantaste el compose del repo por error?"
  echo "Apágalo ya: cd /opt/apps/tfm/repo && docker compose down"
  exit 1
fi

# --- Render configs nginx con TFM_DOMAIN ---
/opt/apps/tfm/scripts/render-nginx-config.sh

# Cargar TFM_DOMAIN para mensaje final
# shellcheck disable=SC1091
source /opt/apps/tfm/.env

echo "OK despliegue TFM. Siguiente (edge en 2 tiempos, ver FASE 14):"
echo "  A) conf.d con tfm-http-only.conf (NUNCA tfm.conf sin certs) y up edge"
echo "  B) Certbot, C) restaurar tfm.conf + reload. Sin cert, Nginx entra en bucle."
echo "Verifica: curl -I http://$TFM_DOMAIN (tras A) | nmap TU_IP (solo 22/80/443)"
