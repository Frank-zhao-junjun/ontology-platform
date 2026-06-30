#!/bin/bash
set -Eeuo pipefail

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
PROJECT_DIR="$(cd "$SCRIPT_DIR/.." && pwd)"
cd "$PROJECT_DIR"

echo "[INFO] Building ontology-platform project..."

if [ -x "./mvnw" ]; then
  ./mvnw clean package -DskipTests -q
else
  mvn clean package -DskipTests -q
fi

if [ $? -eq 0 ]; then
  echo "[INFO] Build successful!"
else
  echo "[ERROR] Build failed!"
  exit 1
fi
