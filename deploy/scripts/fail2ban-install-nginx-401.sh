#!/usr/bin/env bash
# Instala la jail nginx-401 en el HOST (Fail2ban va en host, no en Docker).
# Uso: sudo ./scripts/fail2ban-install-nginx-401.sh
set -euo pipefail
SRC_DIR="$(cd "$(dirname "$0")/.." && pwd)"
sudo cp "$SRC_DIR/fail2ban/filter.d/nginx-401.conf" /etc/fail2ban/filter.d/nginx-401.conf
sudo cp "$SRC_DIR/fail2ban/jail.d/nginx-401.local" /etc/fail2ban/jail.d/nginx-401.local
# Ajusta logpath si tu /opt/apps no es ese:
# sudo nano /etc/fail2ban/jail.d/nginx-401.local
sudo fail2ban-regex /opt/apps/edge/logs/access.log /etc/fail2ban/filter.d/nginx-401.conf || true
sudo systemctl restart fail2ban
sudo fail2ban-client status nginx-401
echo "Desbanear una IP de prueba: sudo fail2ban-client set nginx-401 unbanip TU_IP"
