#!/usr/bin/env bash
# Corre esto una vez contra tu cluster, ANTES de aplicar los manifests.
# Ningun valor real queda escrito en este script ni en ningun YAML - por
# eso el secreto se lee de una variable de entorno en vez de venir escrito
# aqui adentro.
set -e

if [ -z "$INTERNAL_CLIENT_SECRET" ]; then
  echo "Exporta INTERNAL_CLIENT_SECRET antes de correr este script."
  echo "Ejemplo: export INTERNAL_CLIENT_SECRET=\$(openssl rand -base64 24)"
  echo "Usa un secreto NUEVO para produccion, no reuses el que tienes en application.properties para dev."
  exit 1
fi

kubectl create secret generic jwt-keys \
  --from-file=privateKey.pem=../keys/privateKey.pem \
  --from-file=publicKey.pem=../keys/publicKey.pem

kubectl create secret generic internal-api-key \
  --from-literal=client-secret="$INTERNAL_CLIENT_SECRET"

kubectl create secret generic tls-ms-profile-crud \
  --from-file=cert.pem=../ms-profile-crud/src/main/resources/tls/cert.pem \
  --from-file=key.pem=../ms-profile-crud/src/main/resources/tls/key.pem

kubectl create secret generic tls-ms-profile-query \
  --from-file=cert.pem=../ms-profile-query/src/main/resources/tls/cert.pem \
  --from-file=key.pem=../ms-profile-query/src/main/resources/tls/key.pem

echo "Secrets creados."
