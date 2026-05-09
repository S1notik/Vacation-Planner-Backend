# Vacation Planner — Backend

REST API для управления отпусками сотрудников. Позволяет работодателям создавать команды, управлять заявками на отпуск и балансом дней, а сотрудникам — подавать и отслеживать свои заявки.

---

## Стек

![Java](https://img.shields.io/badge/Java_25-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring_Boot_4-6DB33F?style=for-the-badge&logo=spring&logoColor=white)
![Spring Security](https://img.shields.io/badge/Spring_Security-6DB33F?style=for-the-badge&logo=springsecurity&logoColor=white)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL_18-316192?style=for-the-badge&logo=postgresql&logoColor=white)
![Redis](https://img.shields.io/badge/Redis_7-DC382D?style=for-the-badge&logo=redis&logoColor=white)
![Docker](https://img.shields.io/badge/Docker-2496ED?style=for-the-badge&logo=docker&logoColor=white)
![JWT](https://img.shields.io/badge/JWT-000000?style=for-the-badge&logo=jsonwebtokens&logoColor=white)
![Liquibase](https://img.shields.io/badge/Liquibase-2962FF?style=for-the-badge&logo=liquibase&logoColor=white)
![Maven](https://img.shields.io/badge/Maven-C71A36?style=for-the-badge&logo=apachemaven&logoColor=white)
![Lombok](https://img.shields.io/badge/Lombok-BC4521?style=for-the-badge&logo=lombok&logoColor=white)

---

## Архитектура

Проект построен по принципу **Clean Architecture** с разделением на слои:

```
config/          — конфигурация Spring (Security, Redis)
security/        — JWT фильтр, сервис токенов, blacklist
controller/      — REST контроллеры
service/         — интерфейсы бизнес-логики
service/impl/    — реализация сервисов
model/entity/    — JPA сущности
model/enums/     — перечисления (Role, Status, NotificationType)
repository/      — Spring Data JPA репозитории
dto/             — объекты передачи данных (Request/Response)
exception/       — глобальная обработка ошибок
```

---

## API Endpoints

### Auth
| Метод | URL | Описание |
|---|---|---|
| POST | `/api/auth/register` | Регистрация |
| POST | `/api/auth/login` | Вход |
| POST | `/api/auth/logout` | Выход (blacklist токена) |
| POST | `/api/auth/refresh` | Обновление токена |

### Teams
| Метод | URL | Описание | Роль |
|---|---|---|---|
| POST | `/api/teams` | Создать команду | Любая |
| POST | `/api/teams/join` | Вступить по инвайт-коду | EMPLOYEE |
| GET | `/api/teams/members` | Участники команды | Любая |
| GET | `/api/teams/calendar` | Календарь отпусков | Любая |

### Vacations
| Метод | URL | Описание | Роль |
|---|---|---|---|
| POST | `/api/vacations` | Подать заявку | EMPLOYEE |
| GET | `/api/vacations/my` | Мои заявки | EMPLOYEE |
| GET | `/api/vacations/balance` | Баланс дней | EMPLOYEE |
| GET | `/api/vacations/team` | Все заявки команды | EMPLOYER |
| PUT | `/api/vacations/{id}/review` | Одобрить/отклонить | EMPLOYER |
| DELETE | `/api/vacations/{id}` | Отозвать заявку | EMPLOYEE |
| PUT | `/api/vacations/balance/{employeeId}` | Установить баланс сотруднику | EMPLOYER |
| PUT | `/api/vacations/balance/team` | Установить баланс команде | EMPLOYER |

### Notifications
| Метод | URL | Описание |
|---|---|---|
| GET | `/api/notifications` | Получить уведомления |
| PATCH | `/api/notifications/{id}/read` | Прочитать уведомление |
| PATCH | `/api/notifications/read-all` | Прочитать все |

---

## Запуск проекта

### Требования
- Docker
- Docker Compose

### 1. Клонировать репозиторий
```bash
git clone https://github.com/your-repo/vacation-planner-backend.git
cd vacation-planner-backend
```

### 2. Создать `.env` файл в корне проекта
```env
DB_URL=jdbc:postgresql://db:5432/VacationPlanner
USERNAME=your_db_username
SECRET_PASSWORD=your_db_password
POSTGRES_DB=VacationPlanner
JWT_SECRET=your_256_bit_secret_key
JWT_EXPIRATION=3600000
JWT_REFRESH_EXPIRATION=2592000000
```

### 3. Запустить
```bash
docker compose up --build
```

Приложение будет доступно на `http://localhost:8080`

---

## Безопасность

- **JWT токены** — stateless аутентификация
- **Redis Blacklist** — отзыв токенов при logout
- **BCrypt** — хэширование паролей
- **Role-based access** — разграничение прав через `@PreAuthorize`
- **Validation** — валидация входящих данных через `@Valid`

---

## Роли пользователей

| Роль | Описание |
|---|---|
| `EMPLOYER` | Создаёт команду, управляет заявками и балансом отпусков |
| `EMPLOYEE` | Подаёт заявки, отслеживает статус и баланс |
