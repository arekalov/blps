#!/bin/bash

echo "=== Генерация OpenAPI спецификации ==="

set -e

PORT="${PORT:-8080}"
API_DOCS_URL="http://localhost:${PORT}/blps/v3/api-docs.yaml"
OUTPUT_FILE="docs/openapi.yaml"
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "$SCRIPT_DIR/.." && pwd)"
cd "$PROJECT_ROOT"

echo "Сборка JAR..."
./gradlew bootJar --no-daemon -q

JAR_PATH="build/libs/blps.jar"
if [ ! -f "$JAR_PATH" ]; then
    echo "❌ JAR не найден: $JAR_PATH"
    exit 1
fi

echo "✅ JAR собран: $JAR_PATH"

if curl -sf "$API_DOCS_URL" >/dev/null 2>&1; then
    echo "⚠️  На порту $PORT уже отвечает приложение. Остановите его или задайте PORT=..."
    exit 1
fi

echo "Запуск приложения (java -jar)..."
java -jar "$JAR_PATH" > /tmp/openapi-blps.log 2>&1 &
APP_PID=$!
echo "✅ Процесс Spring Boot (PID: $APP_PID)"

cleanup() {
    echo ""
    echo "Остановка приложения..."
    kill "$APP_PID" 2>/dev/null || true
    wait "$APP_PID" 2>/dev/null || true
    echo "✅ Приложение остановлено"
}
trap cleanup EXIT

echo "Ожидание готовности API..."
for i in $(seq 1 120); do
    if curl -sf "$API_DOCS_URL" >/dev/null 2>&1; then
        echo "✅ Приложение готово!"
        break
    fi
    if ! kill -0 "$APP_PID" 2>/dev/null; then
        echo "❌ Процесс упал. Последние 50 строк лога:"
        tail -n 50 /tmp/openapi-blps.log
        exit 1
    fi
    if [ "$i" -eq 120 ]; then
        echo "❌ Таймаут ожидания. Последние 100 строк лога:"
        tail -n 100 /tmp/openapi-blps.log
        exit 1
    fi
    sleep 1
    echo -n "."
done
echo ""

echo "Скачивание OpenAPI спецификации..."
mkdir -p docs
curl -sf "$API_DOCS_URL" -o "$OUTPUT_FILE"

echo "✅ Спецификация сохранена в $OUTPUT_FILE"

echo ""
echo "=== Первые 20 строк сгенерированного файла ==="
head -n 20 "$OUTPUT_FILE"

echo ""
echo "✅ Готово! Файл $OUTPUT_FILE обновлен"
