#!/bin/bash
# Generate self-signed CA + server/client certs for mTLS
# Phase 2c — MCP<->Platform mutual TLS
set -e

DAYS=365
PASS=changeit
OUTDIR=./certs

mkdir -p "$OUTDIR"

echo "=== Generating CA ==="
openssl req -x509 -newkey rsa:4096 -days $DAYS -nodes \
  -keyout "$OUTDIR/ca-key.pem" -out "$OUTDIR/ca-cert.pem" \
  -subj "/CN=ontology-platform-ca/O=OntologyPlatform/C=CN"

echo "=== Generating Server cert (Spring Boot) ==="
openssl req -newkey rsa:2048 -nodes \
  -keyout "$OUTDIR/server-key.pem" -out "$OUTDIR/server-req.pem" \
  -subj "/CN=platform.ontology.local/O=OntologyPlatform/C=CN"
openssl x509 -req -in "$OUTDIR/server-req.pem" -days $DAYS \
  -CA "$OUTDIR/ca-cert.pem" -CAkey "$OUTDIR/ca-key.pem" -CAcreateserial \
  -out "$OUTDIR/server-cert.pem"

echo "=== Generating Client cert (MCP Server) ==="
openssl req -newkey rsa:2048 -nodes \
  -keyout "$OUTDIR/client-key.pem" -out "$OUTDIR/client-req.pem" \
  -subj "/CN=mcp.ontology.local/O=OntologyPlatform/C=CN"
openssl x509 -req -in "$OUTDIR/client-req.pem" -days $DAYS \
  -CA "$OUTDIR/ca-cert.pem" -CAkey "$OUTDIR/ca-key.pem" -CAcreateserial \
  -out "$OUTDIR/client-cert.pem"

# PKCS12 for Spring Boot
openssl pkcs12 -export \
  -in "$OUTDIR/server-cert.pem" -inkey "$OUTDIR/server-key.pem" \
  -out "$OUTDIR/server-keystore.p12" -password pass:$PASS

# Truststore with CA
keytool -import -trustcacerts -noprompt \
  -file "$OUTDIR/ca-cert.pem" -alias ontology-ca \
  -keystore "$OUTDIR/server-truststore.p12" -storepass $PASS

echo "=== Done ==="
echo "Files in $OUTDIR/:"
ls -la "$OUTDIR/"
