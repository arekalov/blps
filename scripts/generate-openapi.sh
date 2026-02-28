#!/bin/bash

echo "=== Генерация OpenAPI спецификации ==="

PORT=8080
API_DOCS_URL="http://localhost:$PORT/blps/v3/api-docs.yaml"
OUTPUT_FILE="docs/openapi.yaml"

# Проверка переменной окружения WILDFLY_HOME
if [ -z "$WILDFLY_HOME" ]; then
    echo "❌ Переменная WILDFLY_HOME не установлена!"
    echo "   Установите её: export WILDFLY_HOME=/path/to/wildfly"
    exit 1
fi

if [ ! -d "$WILDFLY_HOME" ]; then
    echo "❌ Директория WildFly не найдена: $WILDFLY_HOME"
    exit 1
fi

echo "Сборка WAR..."
./gradlew bootWar --no-daemon -q
if [ $? -ne 0 ]; then
    echo "❌ Ошибка сборки!"
    exit 1
fi

WAR_PATH="build/libs/blps.war"
if [ ! -f "$WAR_PATH" ]; then
    echo "❌ WAR файл не найден!"
    exit 1
fi

echo "✅ WAR собран: $WAR_PATH"

echo "Остановка WildFly (если запущен)..."
pkill -9 -f "jboss-modules.jar" 2>/dev/null || true
sleep 3

echo "Очистка старых deployment файлов..."
rm -f $WILDFLY_HOME/standalone/deployments/blps.war.* 2>/dev/null || true

echo "Копирование WAR в WildFly..."
cp "$WAR_PATH" $WILDFLY_HOME/standalone/deployments/

echo "Запуск WildFly..."
$WILDFLY_HOME/bin/standalone.sh > /tmp/openapi-wildfly.log 2>&1 &
WILDFLY_PID=$!
echo "✅ WildFly запущен (PID: $WILDFLY_PID)"

echo "Ожидание запуска WildFly и деплоя приложения..."
for i in {1..120}; do
    if curl -s "$API_DOCS_URL" > /dev/null 2>&1; then
        echo "✅ Приложение готово!"
        break
    fi
    if ! kill -0 $WILDFLY_PID 2>/dev/null; then
        echo "❌ WildFly упал! Последние 50 строк лога:"
        tail -n 50 /tmp/openapi-wildfly.log
        exit 1
    fi
    if [ $i -eq 120 ]; then
        echo "❌ Приложение не запустилось за 120 секунд. Последние 100 строк лога:"
        tail -n 100 /tmp/openapi-wildfly.log
        kill $WILDFLY_PID 2>/dev/null
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
    kill $WILDFLY_PID 2>/dev/null
    exit 1
fi

echo "✅ Спецификация сохранена в $OUTPUT_FILE"

echo "Остановка WildFly..."
kill $WILDFLY_PID 2>/dev/null
wait $WILDFLY_PID 2>/dev/null
echo "✅ WildFly остановлен"

echo ""
echo "=== Первые 20 строк сгенерированного файла ==="
head -n 20 "$OUTPUT_FILE"

echo ""
echo "✅ Готово! Файл $OUTPUT_FILE обновлен"
echo "   Теперь можно закоммитить изменения:"
echo "   git add $OUTPUT_FILE"
echo "   git commit -m 'docs: update OpenAPI specification'"
echo "   git push"
