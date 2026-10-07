#!/usr/bin/env bash
# Primer certificado Parte I (HOST, Nginx en Docker, SOLO tfm).
# Requiere: DNS A tfm -> IP VPS propagado y Nginx Docker levantado con el bloque :80.
# Uso junior: DOMAINS=tfm.tudominio.es EMAIL=tu@email.com ./scripts/certbot-first-cert.sh
set -euo pipefail

# Cargar variables del .env del TFM (TFM_DOMAIN, CERTBOT_EMAIL)
if [[ -f /opt/apps/tfm/.env ]]; then
  # shellcheck disable=SC1091
  source /opt/apps/tfm/.env
fi

DOMAINS="${DOMAINS:-${TFM_DOMAIN:-tfm.tudominio.es}}"
EMAIL="${EMAIL:-${CERTBOT_EMAIL:-tu-email@example.com}}"
WEBROOT="${WEBROOT:-/var/www/certbot}"

sudo mkdir -p "$WEBROOT" /etc/letsencrypt
sudo chown -R root:root "$WEBROOT"

# Construye "-d a -d b" a partir de DOMAINS
DARGS=()
for d in $DOMAINS; do DARGS+=(-d "$d"); done

sudo certbot certonly --webroot -w "$WEBROOT" "${DARGS[@]}" --email "$EMAIL" --agree-tos --no-eff-email

echo "Verifica:"
echo "  sudo certbot renew --dry-run"
echo "  curl -I https://$TFM_DOMAIN"
echo "La renovación automática la hace el timer de certbot; el reload de Nginx lo hace el deploy-hook (ver renew-hook-reload-nginx.sh)."
