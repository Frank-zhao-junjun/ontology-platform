#!/bin/bash
set -Eeuo pipefail

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
PROJECT_DIR="$(cd "$SCRIPT_DIR/.." && pwd)"
cd "$PROJECT_DIR"

echo "[INFO] Building ontology-platform project..."
echo "[INFO] Working directory: $(pwd)"
echo "[INFO] Java version: $(java -version 2>&1 | head -n 1)"
echo "[INFO] Maven version: $(mvn -version 2>&1 | head -n 1)"

# 配置 Maven 使用阿里云镜像加速
export MAVEN_OPTS="-Xmx2g -XX:+UseG1GC"

# 创建临时 settings.xml 使用阿里云镜像
SETTINGS_FILE=$(mktemp)
cat > "$SETTINGS_FILE" << 'EOF'
<settings>
  <mirrors>
    <mirror>
      <id>aliyunmaven</id>
      <name>阿里云公共仓库</name>
      <url>https://maven.aliyun.com/repository/public</url>
      <mirrorOf>central</mirrorOf>
    </mirror>
  </mirrors>
</settings>
EOF

echo "[INFO] Using Maven settings: $SETTINGS_FILE"

# 使用并行构建加速
# -T 1C = 1 thread per CPU core
# -B = batch mode
# -DskipTests = 跳过测试
# -s = 使用自定义 settings.xml

mvn clean package -T 1C -B -DskipTests -s "$SETTINGS_FILE"

BUILD_RESULT=$?

# 清理临时文件
rm -f "$SETTINGS_FILE"

if [ $BUILD_RESULT -eq 0 ]; then
  echo "[INFO] Build successful!"
  echo "[INFO] JAR file: $(ls -lh ontology-api/target/*.jar 2>/dev/null | head -n 1)"
else
  echo "[ERROR] Build failed!"
  exit 1
fi
