#!/usr/bin/env bash
set -e

# Получить абсолютный путь к корню проекта (родительская директория от deployment/)
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "$SCRIPT_DIR/.." && pwd)"

echo "Building WAR..."
cd "$PROJECT_ROOT"
./gradlew bootWar

echo "Stopping WildFly..."
pkill -f "jboss-modules.jar" 2>/dev/null || true
sleep 2

echo "Copying WAR..."
cp "$PROJECT_ROOT/build/libs/blps.war" $WILDFLY_HOME/standalone/deployments/

echo "Starting WildFly..."
trap "pkill -f 'jboss-modules.jar'; exit 0" INT TERM

$WILDFLY_HOME/bin/standalone.sh
