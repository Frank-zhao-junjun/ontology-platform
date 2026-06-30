#!/bin/bash
set -Eeuo pipefail

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
PROJECT_DIR="$(cd "$SCRIPT_DIR/.." && pwd)"
cd "$PROJECT_DIR"

PORT=5000
SPRING_PROFILE="dev"

usage() {
  echo "Usage: $0 [-p port] [-s spring_profile]"
}

while getopts "p:s:h" opt; do
  case "$opt" in
    p)
      PORT="$OPTARG"
      ;;
    s)
      SPRING_PROFILE="$OPTARG"
      ;;
    h)
      usage
      exit 0
      ;;
    \?)
      echo "Invalid option: -$OPTARG"
      usage
      exit 1
      ;;
  esac
done

JAR_FILE="ontology-api/target/ontology-api-1.0.0-SNAPSHOT.jar"

if [ ! -f "$JAR_FILE" ]; then
  echo "[ERROR] JAR file not found: $JAR_FILE"
  echo "[INFO] Please run scripts/build.sh first"
  exit 1
fi

echo "[INFO] Starting Ontology Platform on port $PORT with profile $SPRING_PROFILE..."

export SERVER_PORT=$PORT
export SPRING_PROFILES_ACTIVE=$SPRING_PROFILE

exec java -jar "$JAR_FILE"
