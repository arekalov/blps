#!/usr/bin/env bash
# Перезапуск blps.jar на сервере (Spring Boot, без WildFly).
set -e

BLPS_DIR="${BLPS_DIR:-$HOME/blps}"
JAR="$BLPS_DIR/blps.jar"
LOG="$BLPS_DIR/app.log"

echo ""
echo "=========================================="
echo "Перезапуск BLPS (java -jar)"
echo "=========================================="

if [ ! -f "$JAR" ]; then
  echo "Ошибка: нет файла $JAR (ожидается после scp из deploy-helios.sh)" >&2
  exit 1
fi

echo "Останавливаем старый процесс blps.jar (если есть)..."
pkill -f "java -jar $JAR" 2>/dev/null || true
pkill -f "java.*blps.jar" 2>/dev/null || true
sleep 2

echo "Запуск: SPRING_PROFILES_ACTIVE=prod java -jar $JAR"
mkdir -p "$BLPS_DIR"
cd "$BLPS_DIR"
export SPRING_PROFILES_ACTIVE="${SPRING_PROFILES_ACTIVE:-prod}"
nohup java -jar "$JAR" >> "$LOG" 2>&1 &
echo "PID: $!"
echo "Лог: $LOG"
sleep 3
tail -n 40 "$LOG" || true
