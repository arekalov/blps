# Архитектура приложения BLPS (ЛР4)

Блок-схема развёртывания и **точки интеграции** встроенного BPM-движка Camunda 7 с остальными подсистемами.

## Схема развёртывания

```mermaid
flowchart TB
    subgraph clients["Клиенты"]
        Browser["Браузер<br/>(работодатель, модератор, админ, гость)"]
    end

    subgraph helios["Сервер приложений WildFly 39 (Helios / локально)"]
        subgraph war["WAR blps.war · context-path /blps"]
            subgraph spring["Spring Boot 3 (embedded)"]
                MVC["Spring MVC<br/>welcome.html, /public/register"]
                Sec["Spring Security<br/>BCrypt, permitAll для Camunda UI"]
                Svc["Слой сервисов<br/>VacancyService, ModerationService,<br/>TariffService, AuthService, UserService…"]
                JPA["Spring Data JPA + Flyway"]
            end

            subgraph camunda["Camunda BPM 7.22 (embedded)"]
                Engine["Process Engine"]
                Repo["Repository Service<br/>(BPMN + формы)"]
                Task["Task / Runtime / History"]
                AuthZ["Authorization Service"]
                IdP["BlpsIdentityProvider<br/>(пользователи и группы из БД)"]
                Delegates["JavaDelegate<br/>(vacancyCreateDelegate, …)"]
                Plugins["Process Engine Plugins<br/>auto-assign, initiator, admin auth"]
            end

            subgraph camunda_ui["Camunda Webapps"]
                Welcome["Welcome"]
                Tasklist["Tasklist + Camunda Forms"]
                Cockpit["Cockpit"]
                Rest["engine-rest (Jersey)"]
            end
        end
    end

    subgraph db["PostgreSQL"]
        AppTables[("Таблицы приложения<br/>users, vacancies, tariffs, skills…")]
        CamundaTables[("Таблицы Camunda<br/>ACT_RE_*, ACT_RU_*, ACT_HI_*…")]
    end

    Browser -->|"HTTP"| MVC
    Browser -->|"HTTP /blps/camunda/app/*"| camunda_ui
    camunda_ui --> Engine
    Engine --> Repo
    Engine --> Task
    Engine --> AuthZ
    Engine --> IdP
    Engine -->|"вызов delegateExpression"| Delegates
    Delegates --> Svc
    MVC --> Svc
    Svc --> JPA
    JPA --> AppTables
    Engine --> CamundaTables
    IdP --> JPA
    Plugins --> Engine
```

## Точки интеграции BPM-фреймворка

| № | Точка | Направление | Реализация в проекте |
|---|--------|-------------|----------------------|
| **1** | **Исполняемая бизнес-логика** | BPMN → приложение | `serviceTask` с `camunda:delegateExpression="${…Delegate}"` — делегаты вызывают те же сервисы, что и REST в ЛР2–3 (`VacancyService`, `ModerationService` и т.д.) |
| **2** | **Пользовательский ввод** | UI → процесс | User Task + `camunda:formKey="camunda-forms:deployment:forms/…"` — Camunda Forms, переменные процесса |
| **3** | **Деплой моделей** | Приложение → Camunda | `BlpsCamundaDeploymentConfig` — ZIP из `bpmn/*.bpmn` и `forms/*.form` в `RepositoryService` |
| **4** | **Идентификация и роли** | БД → Camunda | `BlpsIdentityProvider` + `BlpsUserQuery` / `BlpsGroupQuery` — логин по email/паролю, группы `EMPLOYER`, `MODERATOR`, `ADMIN` |
| **5** | **Авторизация Camunda** | Конфиг → Tasklist/Cockpit | `BlpsCamundaAuthorizationConfig`, `BlpsCamundaTaskFilterConfig`, `AdministratorAuthorizationPlugin` |
| **6** | **Назначение и контекст задач** | Движок → задачи | `AutoAssignTaskListener`, `InitiatorEnrichmentListener` — assignee, `initiatorEmail`, `actorRole` |
| **7** | **Представление результатов** | Сервисы → UI | `CamundaPresentation` → переменная `resultSummary` в формах (markdown-таблицы) |
| **8** | **Персистентность** | Camunda + JPA → БД | Общий PostgreSQL: JPA/Flyway — доменная модель; Engine — свои таблицы `ACT_*` |
| **9** | **DataSource (WildFly)** | Сервер → Spring/Camunda | Профиль `wildfly`: JNDI `java:jboss/datasources/BlpsDS` в `application-wildfly.yaml` |
| **10** | **Вспомогательный HTTP** | Вне процессов | `PublicRegisterController` (`/public/register`), `welcome.html` — регистрация без Tasklist |
| **11** | **REST API движка** | Клиент/интеграции → Engine | `/blps/engine-rest` (Jersey), `BlpsCamundaJerseyConfiguration` |

## Поток данных (упрощённо)

```mermaid
sequenceDiagram
    participant U as Пользователь
    participant T as Tasklist
    participant E as Camunda Engine
    participant D as JavaDelegate
    participant S as VacancyService
    participant DB as PostgreSQL

    U->>T: Старт процесса / выполнение User Task
    T->>E: Complete task / start process
    E->>D: serviceTask (delegate)
    D->>S: бизнес-операция
    S->>DB: JPA
    S-->>D: DTO / результат
    D-->>E: setVariable(resultSummary, …)
    E-->>T: следующая User Task + форма
    T-->>U: Camunda Form
```

## Размещение на Helios

| Компонент | Путь / примечание |
|-----------|------------------|
| WildFly | `~/blps/wildfly-39.0.1.Final` |
| Приложение | `standalone/deployments/blps.war` |
| Spring-профиль | **`prod`** (`-Dspring.profiles.active=prod` в `standalone.conf`) |
| HTTP | **23561** (`port-offset=15481`), туннель `ssh -L 8080:localhost:23561 ifmo` |
| БД | PostgreSQL STUDS `pg:5432/studs` (см. `application-prod.yaml`, генерируется из `deployment/helios.env`) |

Деплой: `./deployment/deploy-helios.sh` — подробнее [deployment/README-helios.md](../deployment/README-helios.md).

## Что сознательно не переносилось в BPMN

| Подсистема | Причина |
|------------|---------|
| JMS / очереди сообщений | Не использовались в варианте; Camunda 7 embedded без брокера |
| Распределённые транзакции (2PC) | По заданию ЛР4 не требуются; транзакции Spring `@Transactional` в сервисах |
| Отдельный REST для всех операций | ЛР4: UI и операции через Camunda Tasklist + формы; остался только публичный `/public/register` |

## Связанные артефакты

- Модели процессов: `src/main/resources/bpmn/*.bpmn`
- Формы: `src/main/resources/forms/*.form`
- Матрица ролей: [ROLES_SPECIFICATION.md](ROLES_SPECIFICATION.md)
- Диаграмма классов (ЛР2–3): [class-diagram.md](class-diagram.md)
