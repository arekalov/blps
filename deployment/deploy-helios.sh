#!/usr/bin/env bash
set -e

# Получить абсолютный путь к корню проекта
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "$SCRIPT_DIR/.." && pwd)"

# Конфигурация
REMOTE_HOST="ifmo"
REMOTE_WILDFLY_PATH="~/blps/wildfly-39.0.1.Final"
REMOTE_SCRIPT_PATH="~/blps/remote-deploy.sh"
LOCAL_WAR="build/libs/blps.war"

echo "=========================================="
echo "=== Локальная сборка WAR файла ==="
echo "=========================================="
cd "$PROJECT_ROOT"
./gradlew clean bootWar

echo ""
echo "=========================================="
echo "=== Копирование файлов на Helios ==="
echo "=========================================="

echo "1. Копируем WAR файл..."
scp "$PROJECT_ROOT/$LOCAL_WAR" "${REMOTE_HOST}:${REMOTE_WILDFLY_PATH}/standalone/deployments/blps.war"

echo ""
echo "2. Копируем скрипт деплоя..."
scp "$SCRIPT_DIR/remote-deploy.sh" "${REMOTE_HOST}:${REMOTE_SCRIPT_PATH}"

echo ""
echo "3. Делаем скрипт исполняемым..."
ssh ${REMOTE_HOST} "chmod +x ${REMOTE_SCRIPT_PATH}"

echo ""
echo "=========================================="
echo "=== Запуск удаленного скрипта деплоя ==="
echo "=========================================="
echo ""

# Запускаем удаленный скрипт
ssh ${REMOTE_HOST} "bash ${REMOTE_SCRIPT_PATH}"

echo ""
echo ""
echo "=========================================="
echo "=== DEPLOYMENT ЗАВЕРШЕН! ==="
echo "=========================================="
echo ""
echo "Настраиваем port forwarding и подключаемся к логам..."
echo "Port forwarding: localhost:8080 -> helios:23561"
echo "Приложение будет доступно по адресу: http://localhost:8080/blps/"
echo ""
echo "Нажмите Ctrl+C для выхода"
echo ""
echo "=========================================="
echo "ЛОГИ СЕРВЕРА (real-time):"
echo "=========================================="

# Создаем SSH туннель с проброской порта и выводом логов
ssh -L 8080:localhost:23561 ${REMOTE_HOST} "tail -f ${REMOTE_WILDFLY_PATH}/standalone/log/server.log"
