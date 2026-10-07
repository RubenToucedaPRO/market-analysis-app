#!/usr/bin/env bash
# Renderiza configs nginx in-place sustituyendo ${TFM_DOMAIN}
set -euo pipefail

# Cargar TFM_DOMAIN desde .env si existe (en VPS siempre existe)
if [[ -f /opt/apps/tfm/.env ]]; then
  # shellcheck disable=SC1091
  source /opt/apps/tfm/.env
fi

# TFM_DOMAIN debe estar definido (en .env o exportado en CI)
: "${TFM_DOMAIN:?TFM_DOMAIN no definido. Defínelo en /opt/apps/tfm/.env o exporta la variable.}"
# envsubst solo ve variables exportadas, no variables de shell sin exportar.
export TFM_DOMAIN

for f in /opt/apps/edge/conf.d/tfm.conf /opt/apps/edge/conf.d/tfm-http-only.conf; do
  # Tras el Paso C solo existe tfm.conf (el http-only se borra); no fallar por eso.
  if [[ ! -f "$f" ]]; then
    echo "Aviso: falta $f, se omite (normal tras el Paso C)."
    continue
  fi
  envsubst '${TFM_DOMAIN}' < "$f" > "$f.tmp" && mv "$f.tmp" "$f"
done