#!/usr/bin/env bash
# Сборка JAR и копирование на удалённый хост (без WildFly).
set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "$SCRIPT_DIR/.." && pwd)"

REMOTE_HOST="ifmo"
REMOTE_BLPS_DIR="~/blps"
REMOTE_SCRIPT_PATH="~/blps/remote-deploy.sh"
LOCAL_JAR="build/libs/blps.jar"

echo "=========================================="
echo "=== Локальная сборка JAR ==="
echo "=========================================="
cd "$PROJECT_ROOT"
./gradlew clean bootJar

echo ""
echo "=========================================="
echo "=== Копирование на Helios ==="
echo "=========================================="

echo "1. Копируем JAR..."
scp "$PROJECT_ROOT/$LOCAL_JAR" "${REMOTE_HOST}:${REMOTE_BLPS_DIR}/blps.jar"

echo ""
echo "2. Копируем скрипт деплоя..."
scp "$SCRIPT_DIR/remote-deploy.sh" "${REMOTE_HOST}:${REMOTE_SCRIPT_PATH}"

echo ""
echo "3. Права на скрипт..."
ssh "${REMOTE_HOST}" "chmod +x ${REMOTE_SCRIPT_PATH}"

echo ""
echo "=========================================="
echo "=== Удалённый деплой ==="
echo "=========================================="

ssh "${REMOTE_HOST}" "bash ${REMOTE_SCRIPT_PATH}"

echo ""
echo "=========================================="
echo "=== DEPLOYMENT ЗАВЕРШЁН ==="
echo "=========================================="
echo ""
echo "Логи приложения на сервере: ~/blps/app.log"
echo "Порт приложения (prod): 23561 — см. application-prod.yaml"
echo "Контекст: /blps — http://localhost:8080/blps/ при пробросе порта"
echo ""
echo "Проброс порта и логи (пример):"
echo "  ssh -L 8080:localhost:23561 ${REMOTE_HOST} tail -f ~/blps/app.log"
