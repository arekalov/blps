#!/usr/bin/env bash
set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "$SCRIPT_DIR/.." && pwd)"

REMOTE_HOST="ifmo"
REMOTE_WILDFLY_PATH="~/blps/wildfly-39.0.1.Final"
REMOTE_RESTART_PATH="~/blps/restart-helios-remote.sh"
LOCAL_WAR="build/libs/blps.war"
HELIOS_ENV="$SCRIPT_DIR/helios.env"
TUNNEL_PID_FILE="$SCRIPT_DIR/.helios-tunnel.pid"
PROD_YAML="$PROJECT_ROOT/src/main/resources/application-prod.yaml"
PROD_EXAMPLE="$SCRIPT_DIR/application-prod.yaml.example"

FOLLOW_LOGS=true
SKIP_TUNNEL=false
TAIL_PID=""
REMOTE_LOG="${REMOTE_WILDFLY_PATH}/standalone/log/server.log"

for arg in "$@"; do
  case "$arg" in
    --logs) FOLLOW_LOGS=true ;;
    --no-logs) FOLLOW_LOGS=false ;;
    --no-tunnel) SKIP_TUNNEL=true ;;
    -h|--help)
      echo "Использование: $0 [--no-logs] [--no-tunnel]"
      echo "  --no-logs    не показывать tail -F server.log (по умолчанию логи идут непрерывно)"
      echo "  --no-tunnel  только сборка и деплой, без SSH port forward"
      exit 0
      ;;
    *)
      echo "Неизвестный аргумент: $arg (см. $0 --help)" >&2
      exit 1
      ;;
  esac
done

stop_tunnel() {
  if [[ -f "$TUNNEL_PID_FILE" ]]; then
    local pid
    pid="$(cat "$TUNNEL_PID_FILE")"
    if kill -0 "$pid" 2>/dev/null; then
      kill "$pid" 2>/dev/null || true
      echo "Остановлен старый туннель (PID $pid)"
    fi
    rm -f "$TUNNEL_PID_FILE"
  fi
}

start_tunnel() {
  local local_port="${HELIOS_LOCAL_PORT:-8080}"
  local remote_port="${HELIOS_REMOTE_PORT:-23561}"

  stop_tunnel

  echo "Проброс портов: localhost:${local_port} -> ${REMOTE_HOST}:localhost:${remote_port}"
  ssh -fN \
    -o ExitOnForwardFailure=yes \
    -o ServerAliveInterval=60 \
    -L "${local_port}:localhost:${remote_port}" \
    "${REMOTE_HOST}"

  sleep 1
  local ssh_pid
  ssh_pid="$(lsof -ti "TCP:${local_port}" -sTCP:LISTEN 2>/dev/null | head -1 || true)"
  if [[ -z "$ssh_pid" ]]; then
    ssh_pid="$(pgrep -f "ssh.*-L ${local_port}:localhost:${remote_port}.*${REMOTE_HOST}" 2>/dev/null | head -1 || true)"
  fi
  if [[ -z "$ssh_pid" ]]; then
    echo "Ошибка: не удалось поднять SSH-туннель" >&2
    exit 1
  fi
  echo "$ssh_pid" > "$TUNNEL_PID_FILE"

  echo ""
  echo "Туннель запущен (PID $ssh_pid)"
  echo "  Welcome:  http://localhost:${local_port}/blps/camunda/app/welcome/"
  echo "  Tasklist: http://localhost:${local_port}/blps/camunda/app/tasklist/"
  echo ""
  echo "Остановить туннель: kill \$(cat deployment/.helios-tunnel.pid)"
}

stop_log_tail() {
  if [[ -n "$TAIL_PID" ]] && kill -0 "$TAIL_PID" 2>/dev/null; then
    kill "$TAIL_PID" 2>/dev/null || true
    wait "$TAIL_PID" 2>/dev/null || true
  fi
  TAIL_PID=""
}

start_log_tail() {
  echo "Логи WildFly (tail -F, Ctrl+C — выход из просмотра логов):"
  echo "=========================================="
  ssh "${REMOTE_HOST}" "tail -F ${REMOTE_LOG} 2>/dev/null || tail -f ${REMOTE_LOG}" &
  TAIL_PID=$!
}

follow_logs_forever() {
  if [[ -n "$TAIL_PID" ]] && kill -0 "$TAIL_PID" 2>/dev/null; then
    echo ""
    echo "Логи продолжаются (Ctrl+C — выход; туннель останется в фоне):"
    wait "$TAIL_PID"
    return
  fi
  start_log_tail
  wait "$TAIL_PID"
}

trap 'stop_log_tail' EXIT

if [[ ! -f "$HELIOS_ENV" ]]; then
  echo "Ошибка: нет $HELIOS_ENV" >&2
  echo "  cp deployment/helios.env.example deployment/helios.env" >&2
  echo "  и укажите STUDS_DB_PASSWORD (см. deployment/README-helios.md)" >&2
  exit 1
fi

# shellcheck source=helios.env
set -a
source "$HELIOS_ENV"
set +a

if [[ -z "${STUDS_DB_USER:-}" || -z "${STUDS_DB_PASSWORD:-}" || -z "${STUDS_DB_SCHEMA:-}" ]]; then
  echo "Ошибка: в helios.env нужны STUDS_DB_USER, STUDS_DB_PASSWORD и STUDS_DB_SCHEMA" >&2
  exit 1
fi

if ! command -v envsubst >/dev/null 2>&1; then
  echo "Ошибка: нужен envsubst (brew install gettext && brew link --force gettext)" >&2
  exit 1
fi

echo "=========================================="
echo "=== Генерация application-prod.yaml ==="
echo "=========================================="
export STUDS_DB_USER STUDS_DB_PASSWORD STUDS_DB_SCHEMA
envsubst '${STUDS_DB_USER} ${STUDS_DB_PASSWORD} ${STUDS_DB_SCHEMA}' < "$PROD_EXAMPLE" > "$PROD_YAML"
echo "OK: $PROD_YAML"

echo ""
echo "=========================================="
echo "=== Локальная сборка WAR ==="
echo "=========================================="
cd "$PROJECT_ROOT"
./gradlew clean bootWar

WAR="$PROJECT_ROOT/$LOCAL_WAR"
if ! unzip -p "$WAR" WEB-INF/classes/application-prod.yaml | grep -q 'pg:5432/studs'; then
  echo "Ошибка: в WAR нет application-prod.yaml с Helios DB." >&2
  exit 1
fi
if ! unzip -p "$WAR" WEB-INF/classes/bpmn/tariff-list.bpmn | grep -q 'camunda-forms:deployment:forms/'; then
  echo "Ошибка: в WAR нет camunda-forms:deployment." >&2
  exit 1
fi
echo "WAR OK: prod profile + Camunda forms"

echo ""
echo "=========================================="
echo "=== Копирование на Helios ==="
echo "=========================================="
scp "$WAR" "${REMOTE_HOST}:${REMOTE_WILDFLY_PATH}/standalone/deployments/blps.war"
scp "$SCRIPT_DIR/restart-helios-remote.sh" "${REMOTE_HOST}:${REMOTE_RESTART_PATH}"
ssh "${REMOTE_HOST}" "chmod +x ${REMOTE_RESTART_PATH}"

echo ""
echo "=========================================="
echo "=== Перезапуск WildFly на Helios ==="
echo "=========================================="
if [[ "$FOLLOW_LOGS" == true ]]; then
  start_log_tail
fi
ssh "${REMOTE_HOST}" "bash ${REMOTE_RESTART_PATH}"

echo ""
echo "=========================================="
echo "=== DEPLOYMENT ЗАВЕРШЁН ==="
echo "=========================================="

if [[ "$SKIP_TUNNEL" == true ]]; then
  echo "Туннель пропущен (--no-tunnel)."
  echo "Вручную: ssh -L ${HELIOS_LOCAL_PORT:-8080}:localhost:${HELIOS_REMOTE_PORT:-23561} ${REMOTE_HOST}"
  if [[ "$FOLLOW_LOGS" == true ]]; then
    follow_logs_forever
  fi
  exit 0
fi

start_tunnel

if [[ "$FOLLOW_LOGS" == true ]]; then
  follow_logs_forever
fi
