# Деплой на Helios (ifmo)

## Что должно быть на сервере

| Путь | Назначение |
|------|------------|
| `~/blps/wildfly-39.0.1.Final` | WildFly 39 |
| `~/blps/wildfly-.../modules/org/postgresql/main/` | JDBC-драйвер PostgreSQL |
| `~/blps/wildfly-.../standalone/configuration/standalone.xml` | DataSource `BlpsDS` → `pg:5432/studs` |
| `~/blps/wildfly-.../bin/standalone.conf` | `-Dspring.profiles.active=prod`, `port-offset=15481` (HTTP **23561**) |

Фрагмент `standalone.conf` (уже на сервере):

```bash
JAVA_OPTS="$JAVA_OPTS -Dspring.profiles.active=prod"
JAVA_OPTS="$JAVA_OPTS -Djboss.socket.binding.port-offset=15481"
```

## С Mac (один раз)

```bash
cp deployment/helios.env.example deployment/helios.env
# заполните STUDS_DB_PASSWORD (как в BlpsDS на Helios)
```

## Деплой

```bash
./deployment/deploy-helios.sh
```

Скрипт: собирает WAR с профилем **prod**, копирует на Helios, перезапускает WildFly через `restart-helios-remote.sh` (без `pkill -9 java`).

## Доступ с ноутбука

```bash
ssh -L 8080:localhost:23561 ifmo
```

- Welcome: http://localhost:8080/blps/camunda/app/welcome/
- Tasklist: http://localhost:8080/blps/camunda/app/tasklist/

Логин: пользователи из БД (bootstrap admin `admin@ya.ru` / `admin`).

## Только перезапуск на Helios

```bash
ssh ifmo 'bash ~/blps/restart-helios-remote.sh'
```

## Профили Spring

| Профиль | Где |
|---------|-----|
| `dev` | локально, Neon |
| `wildfly` | локальный WildFly + JNDI `BlpsDS` |
| `prod` | **Helios**, JDBC `pg:5432/studs` |
