#!/usr/bin/env bash
set -e

# Получить абсолютный путь к корню проекта (родительская директория от deployment/)
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "$SCRIPT_DIR/.." && pwd)"

# Локальный путь к WildFly (не в git): deployment/wildfly.env с строкой WILDFLY_HOME=...
if [[ -z "${WILDFLY_HOME:-}" && -f "$SCRIPT_DIR/wildfly.env" ]]; then
	# shellcheck source=wildfly.env
	set -a
	source "$SCRIPT_DIR/wildfly.env"
	set +a
fi

if [[ -z "${WILDFLY_HOME:-}" ]]; then
	echo "Ошибка: не задан WILDFLY_HOME (корень установки WildFly)." >&2
	echo "  export WILDFLY_HOME=/путь/к/wildfly-XX.X.X.Final" >&2
	echo "или создайте файл deployment/wildfly.env:" >&2
	echo "  WILDFLY_HOME=/путь/к/wildfly-XX.X.X.Final" >&2
	exit 1
fi

WILDFLY_HOME="$(cd "$WILDFLY_HOME" && pwd)"
DEPLOY_DIR="$WILDFLY_HOME/standalone/deployments"
if [[ ! -d "$DEPLOY_DIR" ]]; then
	echo "Ошибка: каталог деплоя не найден: $DEPLOY_DIR" >&2
	echo "Проверьте, что WILDFLY_HOME указывает на корень WildFly (где есть standalone/deployments)." >&2
	exit 1
fi

if [[ ! -x "$WILDFLY_HOME/bin/standalone.sh" ]]; then
	echo "Ошибка: не найден или не исполняемый: $WILDFLY_HOME/bin/standalone.sh" >&2
	exit 1
fi

echo "Building WAR..."
cd "$PROJECT_ROOT"
./gradlew bootWar

echo "Stopping WildFly..."
pkill -f "jboss-modules.jar" 2>/dev/null || true
sleep 2

# Один context-root /blps (jboss-web.xml) — второй WAR даёт WFLYUT0105 DuplicateServiceException
echo "Removing previous blps deployments from $DEPLOY_DIR ..."
shopt -s nullglob
for f in "$DEPLOY_DIR"/blps*.war "$DEPLOY_DIR"/blps*.war.*; do
	if [[ -e "$f" ]]; then
		echo "  rm $f"
		rm -f "$f"
	fi
done
shopt -u nullglob

echo "Copying WAR to $DEPLOY_DIR ..."
cp "$PROJECT_ROOT/build/libs/blps.war" "$DEPLOY_DIR/"

echo "Starting WildFly..."
trap "pkill -f 'jboss-modules.jar'; exit 0" INT TERM

"$WILDFLY_HOME/bin/standalone.sh"
