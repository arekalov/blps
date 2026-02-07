#!/bin/bash

echo "=== Генерация OpenAPI спецификации ==="

PORT=8080
API_DOCS_URL="http://localhost:$PORT/v3/api-docs.yaml"
OUTPUT_FILE="docs/openapi.yaml"

echo "Сборка приложения..."
./gradlew bootJar --no-daemon -q
if [ $? -ne 0 ]; then
    echo "❌ Ошибка сборки!"
    exit 1
fi

JAR_PATH=$(find build/libs -name "*-SNAPSHOT.jar" ! -name "*-plain.jar" | head -n 1)
if [ -z "$JAR_PATH" ]; then
    echo "❌ JAR файл не найден!"
    exit 1
fi

echo "✅ JAR собран: $JAR_PATH"

echo "Запуск приложения (порт $PORT)..."
java -jar "$JAR_PATH" > /tmp/openapi-gen.log 2>&1 &
APP_PID=$!
echo "✅ Приложение запущено (PID: $APP_PID)"

echo "Ожидание запуска..."
for i in {1..60}; do
    if curl -s "$API_DOCS_URL" > /dev/null 2>&1; then
        echo "✅ Приложение готово!"
        break
    fi
    if ! kill -0 $APP_PID 2>/dev/null; then
        echo "❌ Приложение упало! Последние 50 строк лога:"
        tail -n 50 /tmp/openapi-gen.log
        exit 1
    fi
    if [ $i -eq 60 ]; then
        echo "❌ Приложение не запустилось за 60 секунд. Последние 50 строк лога:"
        tail -n 50 /tmp/openapi-gen.log
        kill $APP_PID 2>/dev/null
        exit 1
    fi
    sleep 1
    echo -n "."
done
echo ""

echo "Скачивание OpenAPI спецификации..."
mkdir -p docs
curl -s "$API_DOCS_URL" -o "$OUTPUT_FILE"
if [ $? -ne 0 ]; then
    echo "❌ Ошибка скачивания!"
    kill $APP_PID 2>/dev/null
    exit 1
fi

echo "✅ Спецификация сохранена в $OUTPUT_FILE"

echo "Остановка приложения..."
kill $APP_PID 2>/dev/null
wait $APP_PID 2>/dev/null
echo "✅ Приложение остановлено"

echo ""
echo "=== Первые 20 строк сгенерированного файла ==="
head -n 20 "$OUTPUT_FILE"

echo ""
echo "✅ Готово! Файл $OUTPUT_FILE обновлен"
echo "   Теперь можно закоммитить изменения:"
echo "   git add $OUTPUT_FILE"
echo "   git commit -m 'docs: update OpenAPI specification'"
echo "   git push"
