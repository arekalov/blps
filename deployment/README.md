# Deployment Guide

Скрипт для деплоя Spring Boot приложения на сервер Гелиос ИТМО.

## Использование

```bash
./deployment/deploy_and_start.sh
```

Скрипт выполняет полный цикл: сборку JAR, деплой на сервер и запуск с пробросом порта.

**Остановка:** Нажмите `Ctrl+C`

## Настройка

1. Настройте SSH в `~/.ssh/config`:
```
Host ifmo
    HostName se.ifmo.ru
    User sXXXXXX
    Port 2222
```

2. Создайте `deployment/application-prod.yaml`:
```bash
cp deployment/application-prod.yaml.example deployment/application-prod.yaml
```

3. Заполните credentials для STUDS базы данных

Подробности в основном README.md
