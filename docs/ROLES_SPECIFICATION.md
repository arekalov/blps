# Спецификация ролей и привилегий

## Матрица доступа к REST API

### Управление вакансиями


| Endpoint                               | PUBLIC | EMPLOYER  | MODERATOR | ADMIN |
| -------------------------------------- | ------ | --------- | --------- | ----- |
| `GET /api/v1/vacancies`                | ✅      | ✅         | ✅         | ✅     |
| `GET /api/v1/vacancies/{id}`           | ✅      | ✅         | ✅         | ✅     |
| `POST /api/v1/vacancies`               | ❌      | ✅         | ❌         | ✅     |
| `PATCH /api/v1/vacancies/{id}`         | ❌      | ✅ (owner) | ❌         | ✅     |
| `DELETE /api/v1/vacancies/{id}`        | ❌      | ✅ (owner) | ❌         | ✅     |
| `PATCH /api/v1/vacancies/{id}/tariff`  | ❌      | ✅ (owner) | ❌         | ✅     |
| `PATCH /api/v1/vacancies/{id}/publish` | ❌      | ✅ (owner) | ❌         | ✅     |
| `PATCH /api/v1/vacancies/{id}/archive` | ❌      | ✅ (owner) | ❌         | ✅     |


### Модерация


| Endpoint                               | PUBLIC | EMPLOYER | MODERATOR | ADMIN |
| -------------------------------------- | ------ | -------- | --------- | ----- |
| `GET /api/v1/moderation/pending`       | ❌      | ❌        | ✅         | ✅     |
| `POST /api/v1/moderation/{id}/approve` | ❌      | ❌        | ✅         | ✅     |
| `POST /api/v1/moderation/{id}/reject`  | ❌      | ❌        | ✅         | ✅     |


### Тарифы


| Endpoint                      | PUBLIC | EMPLOYER | MODERATOR | ADMIN |
| ----------------------------- | ------ | -------- | --------- | ----- |
| `GET /api/v1/tariffs`         | ✅      | ✅        | ✅         | ✅     |
| `GET /api/v1/tariffs/{id}`    | ✅      | ✅        | ✅         | ✅     |
| `POST /api/v1/tariffs`        | ❌      | ❌        | ❌         | ✅     |
| `PUT /api/v1/tariffs/{id}`    | ❌      | ❌        | ❌         | ✅     |
| `DELETE /api/v1/tariffs/{id}` | ❌      | ❌        | ❌         | ✅     |


### Статистика тарифов


| Endpoint                              | PUBLIC | EMPLOYER | MODERATOR | ADMIN |
| ------------------------------------- | ------ | -------- | --------- | ----- |
| `GET /api/v1/tariffs/{id}/statistics` | ❌      | ❌        | ✅         | ✅     |
| `GET /api/v1/tariffs/{id}/history`    | ❌      | ❌        | ✅         | ✅     |


### Управление пользователями


| Endpoint                    | PUBLIC | EMPLOYER    | MODERATOR   | ADMIN |
| --------------------------- | ------ | ----------- | ----------- | ----- |
| `GET /api/v1/users`         | ❌      | ✅ (my=true) | ✅ (my=true) | ✅     |
| `GET /api/v1/users/{id}`    | ❌      | ✅ (self)    | ✅ (self)    | ✅     |
| `PATCH /api/v1/users/{id}`  | ❌      | ✅ (self)    | ✅ (self)    | ✅     |
| `DELETE /api/v1/users/{id}` | ❌      | ❌           | ❌           | ✅     |


### Аутентификация


| Endpoint                     | PUBLIC | EMPLOYER | MODERATOR | ADMIN |
| ---------------------------- | ------ | -------- | --------- | ----- |
| `POST /api/v1/auth/register` | ✅      | ✅        | ✅         | ✅     |

