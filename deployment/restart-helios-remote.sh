#!/usr/bin/env bash
# Мягкий перезапуск WildFly на Helios (без pkill -9 java).
set -e

WILDFLY_PATH="${WILDFLY_PATH:-$HOME/blps/wildfly-39.0.1.Final}"
DEPLOY_DIR="$WILDFLY_PATH/standalone/deployments"

echo "=== Helios: перезапуск WildFly ==="
echo "WildFly: $WILDFLY_PATH"

if [[ ! -f "$DEPLOY_DIR/blps.war" ]]; then
  echo "Ошибка: нет $DEPLOY_DIR/blps.war — сначала выполните deploy-helios.sh с Mac." >&2
  exit 1
fi

echo "Останавливаем WildFly (если запущен)..."
cd "$WILDFLY_PATH"
if ./bin/jboss-cli.sh --connect --command=shutdown 2>/dev/null; then
  echo "CLI shutdown отправлен, ждём 15 с..."
  sleep 15
fi

pkill -f "$WILDFLY_PATH/bin/standalone.sh" 2>/dev/null || true
pkill -f "org.jboss.as.standalone.*$WILDFLY_PATH" 2>/dev/null || true
sleep 3

echo "Очищаем маркеры деплоя..."
cd "$DEPLOY_DIR"
rm -f blps.war.failed blps.war.isdeploying blps.war.dodeploy blps.war.deployed 2>/dev/null || true

echo "Запускаем WildFly..."
cd "$WILDFLY_PATH"
nohup ./bin/standalone.sh >> "$WILDFLY_PATH/wildfly.log" 2>&1 &
echo "PID: $!"
echo "Ждём старт (20 с)..."
sleep 20

if ! pgrep -f "$WILDFLY_PATH/bin/standalone.sh" >/dev/null; then
  echo "WildFly не стартовал — смотрите лог:" >&2
  tail -n 40 "$WILDFLY_PATH/standalone/log/server.log" >&2 || tail -n 40 "$WILDFLY_PATH/wildfly.log" >&2
  exit 1
fi

echo "WildFly запущен. Ждём деплой blps.war (до 120 с)..."
for _ in $(seq 1 24); do
  if [[ -f "$DEPLOY_DIR/blps.war.deployed" ]]; then
    echo "blps.war.deployed — OK"
    echo "HTTP: http://127.0.0.1:23561/blps/"
    tail -n 15 "$WILDFLY_PATH/standalone/log/server.log" || true
    exit 0
  fi
  if [[ -f "$DEPLOY_DIR/blps.war.failed" ]]; then
    echo "blps.war.failed — ошибка деплоя:" >&2
    tail -n 40 "$WILDFLY_PATH/standalone/log/server.log" >&2
    exit 1
  fi
  sleep 5
done

echo "Таймаут ожидания blps.war.deployed — проверьте лог:" >&2
tail -n 40 "$WILDFLY_PATH/standalone/log/server.log" >&2
exit 1
