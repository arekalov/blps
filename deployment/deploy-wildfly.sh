#!/usr/bin/env bash
# Локальный запуск Spring Boot JAR (раньше: сборка WAR и WildFly).
set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "$SCRIPT_DIR/.." && pwd)"
cd "$PROJECT_ROOT"

echo "Сборка blps.jar (main-service)..."
./gradlew :main-service:bootJar

JAR="$PROJECT_ROOT/main-service/build/libs/blps.jar"
if [ ! -f "$JAR" ]; then
  echo "Ошибка: не найден $JAR" >&2
  exit 1
fi

echo "Запуск: java -jar $JAR"
echo "  Профиль: задайте SPRING_PROFILES_ACTIVE при необходимости (по умолчанию из application.yaml)."
trap 'echo ""; echo "Остановка (Ctrl+C)..."; exit 0' INT TERM

exec java -jar "$JAR" "$@"
