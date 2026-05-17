#!/usr/bin/env bash
set -e

# Конфигурация
WILDFLY_PATH="$HOME/blps/wildfly-39.0.1.Final"

echo ""
echo "=========================================="
echo "ПРЕДВАРИТЕЛЬНАЯ ПРОВЕРКА: Все процессы"
echo "=========================================="
echo "Показываем текущие процессы (ps aux):"
ps aux | head -30

echo ""
echo "=========================================="
echo "УБИВАЕМ ВСЕ ПРОЦЕССЫ (кроме SSH)"
echo "=========================================="
echo "Останавливаем все процессы Java, WildFly и другие..."

# Получаем PID текущей SSH сессии
CURRENT_SSH_PID=$$

# Убиваем все Java процессы
pkill -9 java 2>/dev/null || true

# Убиваем все WildFly процессы
pkill -9 -f wildfly 2>/dev/null || true
pkill -9 -f standalone 2>/dev/null || true

# Убиваем все остальные процессы пользователя, кроме SSH и текущего shell
for pid in $(ps -u $(whoami) -o pid= | grep -v $$ | grep -v $PPID); do
  # Проверяем что это не sshd
  if ! ps -p $pid -o comm= | grep -q sshd; then
    kill -9 $pid 2>/dev/null || true
  fi
done

echo "Процессы остановлены"

echo ""
echo "Ждем завершения всех процессов..."
sleep 3

echo ""
echo "Проверяем оставшиеся процессы (кроме SSH/bash):"
ps aux | grep $(whoami) | grep -v 'grep\|sshd\|bash\|ps\|ssh' || echo "Все процессы успешно остановлены!"

echo ""
echo "=========================================="
echo "Шаг 2: Проверьте, что WAR файл скопирован"
echo "=========================================="
echo "Проверяем наличие WAR файла:"
ls -lh ${WILDFLY_PATH}/standalone/deployments/
echo "Должен быть blps.war с временем около $(date '+%H:%M')."

echo ""
echo "=========================================="
echo "Шаг 3: Проверьте статус WildFly"
echo "=========================================="
echo "Проверяем процессы wildfly:"
ps aux | grep wildfly | grep -v grep || echo "# нет процессов wildfly"
echo ""
echo "Проверяем процессы java:"
ps aux | grep java | grep -v grep || echo "# нет процессов java"

echo ""
echo "=========================================="
echo "Шаг 4: Остановите WildFly (если запущен)"
echo "=========================================="
echo "Пробуем остановить через jboss-cli..."
cd ${WILDFLY_PATH} && ./bin/jboss-cli.sh --connect --command=shutdown 2>/dev/null || echo "CLI не сработал"

echo ""
echo "Жесткая остановка через pkill (если нужно):"
pkill -f wildfly 2>/dev/null || echo "# нет процессов для остановки"
pkill -f standalone.sh 2>/dev/null || echo "# нет standalone.sh процессов"

echo ""
echo "Ждем завершения процессов..."
sleep 5

echo ""
echo "=========================================="
echo "Шаг 5: Очистите старые deployment файлы"
echo "=========================================="
echo "Переходим в директорию deployments и очищаем:"
cd ${WILDFLY_PATH}/standalone/deployments
rm -f blps.war.* 2>/dev/null || true
rm -f blps.war.deployed 2>/dev/null || true
rm -f blps.war.failed 2>/dev/null || true
echo "Очистка завершена"

echo ""
echo "Проверяем что осталось только blps.war:"
ls -la | grep blps || echo "только blps.war"

echo ""
echo "=========================================="
echo "Шаг 6: Запустите WildFly заново"
echo "=========================================="
echo "Запускаем WildFly в фоне..."
cd ${WILDFLY_PATH}
nohup ./bin/standalone.sh > wildfly.log 2>&1 &
WILDFLY_PID=$!

echo "WildFly запущен с PID: $WILDFLY_PID"
echo ""
echo "Ждем запуска WildFly (15 секунд)..."
sleep 15

echo ""
echo "Проверяем что WildFly запустился:"
ps aux | grep standalone | grep -v grep || echo "WildFly не найден!"

echo ""
echo "=========================================="
echo "Шаг 7: Следите за логами"
echo "=========================================="
echo ""
echo "Deployment завершен!"
echo "Последние 50 строк лога:"
echo "=========================================="
tail -n 50 ${WILDFLY_PATH}/standalone/log/server.log
