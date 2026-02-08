#!/usr/bin/env bash

# Скрипт для деплоя и запуска приложения на сервере Гелиос
# Использование: ./deploy_and_start.sh

set -e  # Остановиться при ошибке

# Цвета для вывода
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

echo -e "${GREEN}=== Деплой и запуск BLPS ===${NC}\n"

# Определение корневой директории проекта
SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
PROJECT_ROOT="$( cd "$SCRIPT_DIR/.." && pwd )"

echo -e "Корневая директория проекта: ${PROJECT_ROOT}"
cd "$PROJECT_ROOT"

# Параметры
SSH_HOST="ifmo"
REMOTE_DIR="~/blps"
APP_NAME="blps-0.0.1-SNAPSHOT.jar"
REMOTE_PORT="23561"
LOCAL_PORT="8080"

# ============================================
# ЭТАП 1: СБОРКА JAR ФАЙЛА
# ============================================
echo -e "${YELLOW}[1/5] Сборка JAR файла...${NC}"
./gradlew clean bootJar
if [ $? -ne 0 ]; then
    echo -e "${RED}Ошибка при сборке JAR файла${NC}"
    exit 1
fi
echo -e "${GREEN}✓ JAR файл собран${NC}\n"

# ============================================
# ЭТАП 2: ОСТАНОВКА СТАРОГО ПРИЛОЖЕНИЯ
# ============================================
echo -e "${YELLOW}[2/5] Остановка старого приложения...${NC}"
ssh $SSH_HOST "
    # Убить процесс по имени JAR файла
    pkill -f '$APP_NAME' || true
    
    # Убить все Java процессы пользователя (осторожно!)
    pkill -9 -u \$USER java || true
    
    # Подождать завершения процессов
    sleep 2
    
    # Проверить, остались ли Java процессы
    if pgrep -u \$USER java > /dev/null; then
        echo 'Предупреждение: некоторые Java процессы все еще работают'
        pgrep -u \$USER -a java
    else
        echo 'Все Java процессы успешно остановлены'
    fi
"
echo -e "${GREEN}✓ Старое приложение остановлено${NC}\n"

# ============================================
# ЭТАП 3: ОЧИСТКА ДИСКА
# ============================================
echo -e "${YELLOW}[3/5] Очистка диска и подготовка директории...${NC}"
ssh $SSH_HOST "
    echo 'Очистка временных файлов и логов...'
    
    # Удаление ВСЕХ старых JAR файлов (освобождаем место!)
    rm -f $REMOTE_DIR/*.jar 2>/dev/null || true
    
    # Очистка логов приложения (если есть)
    rm -rf $REMOTE_DIR/logs/* 2>/dev/null || true
    rm -f $REMOTE_DIR/*.log 2>/dev/null || true
    rm -f $REMOTE_DIR/nohup.out 2>/dev/null || true
    
    # Очистка старых конфигов (кроме application-prod.yaml)
    find $REMOTE_DIR -name '*.yaml.bak' -delete 2>/dev/null || true
    find $REMOTE_DIR -name '*.yaml.old' -delete 2>/dev/null || true
    
    # Очистка временных файлов
    rm -rf $REMOTE_DIR/tmp/* 2>/dev/null || true
    rm -rf /tmp/spring-boot-* 2>/dev/null || true
    rm -rf /tmp/tomcat* 2>/dev/null || true
    rm -rf /tmp/hsperfdata_* 2>/dev/null || true
    
    # Очистка кэша Gradle (если есть)
    rm -rf ~/.gradle/caches/modules-2/files-2.1/*/blps* 2>/dev/null || true
    rm -rf ~/.gradle/caches/jars-* 2>/dev/null || true
    
    # Создать директорию если не существует
    mkdir -p $REMOTE_DIR 2>/dev/null || true
    
    # Показать использование диска
    echo ''
    echo 'Освобождено место. Использование диска:'
    df -h ~ 2>/dev/null || du -sh ~ 2>/dev/null || echo 'Не удалось получить информацию о диске'
    
    echo 'Очистка завершена'
"
echo -e "${GREEN}✓ Диск очищен, директория подготовлена${NC}\n"

# ============================================
# ЭТАП 4: ЗАГРУЗКА ФАЙЛОВ НА СЕРВЕР
# ============================================
echo -e "${YELLOW}[4/5] Загрузка файлов на сервер...${NC}"
scp build/libs/$APP_NAME $SSH_HOST:$REMOTE_DIR/
echo -e "${GREEN}✓ Файлы загружены${NC}\n"

# ============================================
# ЭТАП 5: ЗАПУСК ПРИЛОЖЕНИЯ
# ============================================
echo -e "${YELLOW}[5/5] Запуск приложения...${NC}\n"
echo -e "${GREEN}=== Приложение запущено ===${NC}"
echo ""
echo "Приложение будет доступно на: http://localhost:$LOCAL_PORT"
echo "API документация: http://localhost:$LOCAL_PORT/v3/api-docs"
echo "Swagger UI: http://localhost:$LOCAL_PORT/swagger-ui.html"
echo ""
echo "Нажмите Ctrl+C для остановки"
echo ""

# Функция для очистки при выходе
cleanup() {
    echo ""
    echo "Остановка приложения на сервере..."
    ssh $SSH_HOST "pkill -f '$APP_NAME' || true"
    echo "Готово!"
    exit 0
}

trap cleanup INT TERM

# Проверка, не занят ли порт
if lsof -Pi :$LOCAL_PORT -sTCP:LISTEN -t >/dev/null 2>&1 ; then
    echo -e "${RED}⚠️  Порт $LOCAL_PORT уже занят!${NC}"
    echo "Освободите порт или измените LOCAL_PORT в скрипте"
    exit 1
fi

# Запуск приложения на сервере в фоне и проброс порта
ssh -L $LOCAL_PORT:localhost:$REMOTE_PORT $SSH_HOST \
    "cd $REMOTE_DIR && java -Xmx512m -Xms256m -jar $APP_NAME \
    --spring.profiles.active=prod"
