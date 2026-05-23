#!/usr/bin/env bash
set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "$SCRIPT_DIR/.." && pwd)"

REMOTE_HOST="ifmo"
REMOTE_WILDFLY_PATH="~/blps/wildfly-39.0.1.Final"
REMOTE_RESTART_PATH="~/blps/restart-helios-remote.sh"
LOCAL_WAR="build/libs/blps.war"
HELIOS_ENV="$SCRIPT_DIR/helios.env"
PROD_YAML="$PROJECT_ROOT/src/main/resources/application-prod.yaml"
PROD_EXAMPLE="$SCRIPT_DIR/application-prod.yaml.example"

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
ssh "${REMOTE_HOST}" "bash ${REMOTE_RESTART_PATH}"

echo ""
echo "=========================================="
echo "=== DEPLOYMENT ЗАВЕРШЁН ==="
echo "=========================================="
echo ""
echo "Туннель:  ssh -L 8080:localhost:23561 ${REMOTE_HOST}"
echo "Welcome:  http://localhost:8080/blps/camunda/app/welcome/"
echo ""
echo "Логи (Ctrl+C для выхода):"
echo "=========================================="
ssh -L 8080:localhost:23561 "${REMOTE_HOST}" "tail -f ${REMOTE_WILDFLY_PATH}/standalone/log/server.log"
