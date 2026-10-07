#!/usr/bin/env bash
# Backup MySQL/MariaDB del TFM -> /opt/backups/mariadb-YYYYmmdd-HHMM.sql.gz (retención 7 días)
# Nombres REALES de tu proyecto: contenedor market-analysis-mysql, vars DB_* del .env.
# Uso: /opt/apps/tfm/backup-mariadb.sh  (copia este fichero ahí, chmod +x)
# Cron ejemplo (2:30 AM; log en /opt/backups porque deploy no escribe en /var/log):
# 30 2 * * * /opt/apps/tfm/backup-mariadb.sh >> /opt/backups/backup-cron.log 2>&1
set -euo pipefail

BACKUP_DIR="${BACKUP_DIR:-/opt/backups}"
RETENTION_DAYS="${RETENTION_DAYS:-7}"
ENV_FILE="${ENV_FILE:-/opt/apps/tfm/.env}"

# shellcheck disable=SC1090
set -a; source "$ENV_FILE"; set +a

: "${DB_USER:?falta DB_USER en $ENV_FILE}"
: "${DB_PASSWORD:?falta DB_PASSWORD en $ENV_FILE}"
: "${DB_DATABASE:?falta DB_DATABASE en $ENV_FILE}"

TS="$(date +%Y%m%d-%H%M)"
OUT="$BACKUP_DIR/mariadb-$TS.sql.gz"
mkdir -p "$BACKUP_DIR"

# Dump dentro del contenedor "market-analysis-mysql", comprimido fuera.
docker exec market-analysis-mysql mariadb-dump -u"${DB_USER}" -p"${DB_PASSWORD}" "${DB_DATABASE}" | gzip > "$OUT"
chmod 600 "$OUT"

# Retención local (el envío externo se documenta en la guía)
find "$BACKUP_DIR" -name 'mariadb-*.sql.gz' -mtime +"$RETENTION_DAYS" -delete

# Verificación de integridad
gzip -t "$OUT" || { echo "ERROR: backup corrupto"; exit 1; }
CHECKSUM="$(sha256sum "$OUT" | cut -d' ' -f1)"

echo "OK backup: $OUT ($(du -h "$OUT" | cut -f1))"
echo "SHA256: $CHECKSUM"
echo "Descárgalo a tu PC con (DESDE tu PC, no dentro del VPS):"
VPS_HOST="${VPS_HOST:-141.94.250.163}"
echo "  scp 'deploy@$VPS_HOST:$OUT' ./"
echo "Con alias: scp 'vps-tfm:$OUT' ./"
echo "Y verifica: gzip -t $(basename "$OUT") && echo COPIA OK"
echo "Backup solo local NO es suficiente."
