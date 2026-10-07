#!/usr/bin/env bash
# Deploy-hook de Certbot: recarga Nginx Docker tras renovar.
# Instalar una vez: sudo cp renew-hook-reload-nginx.sh /etc/letsencrypt/renewal-hooks/deploy/reload-nginx-docker.sh
# Certbot lo ejecuta solo cuando un certificado se renueva de verdad.
set -euo pipefail
cd /opt/apps/edge
docker compose exec nginx nginx -s reload
