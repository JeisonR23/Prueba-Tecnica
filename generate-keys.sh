#!/usr/bin/env bash
set -e

mkdir -p keys
openssl genpkey -algorithm RSA -out keys/privateKey.pem -pkeyopt rsa_keygen_bits:2048
openssl rsa -pubout -in keys/privateKey.pem -out keys/publicKey.pem
cp keys/privateKey.pem keys/publicKey.pem ms-profile-crud/src/main/resources/
cp keys/publicKey.pem ms-profile-query/src/main/resources/

mkdir -p ms-profile-crud/src/main/resources/tls
openssl req -newkey rsa:2048 -new -nodes -x509 -days 3650 \
  -keyout ms-profile-crud/src/main/resources/tls/key.pem \
  -out ms-profile-crud/src/main/resources/tls/cert.pem \
  -subj "//CN=localhost"

mkdir -p ms-profile-query/src/main/resources/tls
openssl req -newkey rsa:2048 -new -nodes -x509 -days 3650 \
  -keyout ms-profile-query/src/main/resources/tls/key.pem \
  -out ms-profile-query/src/main/resources/tls/cert.pem \
  -subj "//CN=localhost"

echo "Llaves y certificados generados para los dos microservicios."
