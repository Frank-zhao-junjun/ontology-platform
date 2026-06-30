#!/bin/bash
set -Eeuo pipefail

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
PROJECT_DIR="$(cd "$SCRIPT_DIR/.." && pwd)"
cd "$PROJECT_DIR"

echo "[INFO] Building ontology-platform project..."
echo "[INFO] Working directory: $(pwd)"
echo "[INFO] Java version: $(java -version 2>&1 | head -n 1)"
echo "[INFO] Maven version: $(mvn -version 2>&1 | head -n 1)"

# 使用并行构建加速（每个 CPU 核心一个线程）
# -T 1C = 1 thread per CPU core
# -B = batch mode (减少输出)
# -DskipTests = 跳过测试
# -q = quiet mode

if [ -x "./mvnw" ]; then
  ./mvnw clean package -T 1C -B -DskipTests -q
else
  mvn clean package -T 1C -B -DskipTests -q
fi

if [ $? -eq 0 ]; then
  echo "[INFO] Build successful!"
  echo "[INFO] JAR file: $(ls -lh ontology-api/target/*.jar 2>/dev/null | head -n 1)"
else
  echo "[ERROR] Build failed!"
  exit 1
fi
