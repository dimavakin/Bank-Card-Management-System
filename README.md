
# 💳 Bank Card Management System (REST API)

## 📘 Описание

Система управления банковскими картами, реализованная на **Spring Boot** с поддержкой **JWT-аутентификации** и **ролевого доступа**.  
API предоставляет функционал для:
- управления пользователями и картами (ADMIN),
- работы со своими картами и переводами (USER).

---

## 🚀 Технологии

- **Java 17+**
- **Spring Boot / Spring Security / Spring Data JPA**
- **JWT**
- **PostgreSQL**
- **Liquibase**
- **Docker Compose**
- **Swagger**

---

## 🔐 Роли и доступ

| Роль   | Возможности |
|--------|--------------|
| **ADMIN** | Управление пользователями и всеми картами |
| **USER**  | Доступ к своим картам, переводы и запросы блокировки |

---
## 🧾 Эндпоинты

### 🔒 Auth Controller

| Метод | URL | Описание |
|--------|-----|-----------|
| **POST** | `/api/auth/sign-in` | Аутентификация пользователя |
| **POST** | `/api/auth/refresh` | Обновление access-токена |

---

### 👤 Admin Controller

| Метод | URL | Описание |
|--------|-----|-----------|
| **POST** | `/api/admin/user` | Создание нового пользователя |
| **PUT** | `/api/admin/{userId}` | Обновление данных пользователя |
| **DELETE** | `/api/admin/{userId}` | Удаление пользователя |

---

### 💳 Admin Card Controller

| Метод | URL | Описание |
|--------|-----|-----------|
| **POST** | `/api/admin/card/{userId}` | Создание карты для пользователя |
| **GET** | `/api/admin/card` | Получение всех карт (фильтрация и пагинация) |
| **GET** | `/api/admin/card/user/{userId}` | Получение карт конкретного пользователя |
| **PATCH** | `/api/admin/card/{cardId}/block` | Блокировка карты |
| **PATCH** | `/api/admin/card/{cardId}/activate` | Активация карты |
| **DELETE** | `/api/admin/card/{cardId}` | Удаление карты |

---

### 🧍‍♂️ User Card Controller

| Метод | URL | Описание |
|--------|-----|-----------|
| **GET** | `/api/user/cards` | Получение списка карт пользователя (с фильтрацией) |
| **GET** | `/api/user/cards/{cardId}` | Получение информации о конкретной карте |
| **GET** | `/api/user/cards/balance` | Общий баланс по всем картам пользователя |
| **POST** | `/api/user/cards/{cardId}/block-request` | Запрос на блокировку карты |

---

### 💸 Transfer Controller

| Метод | URL | Описание |
|--------|-----|-----------|
| **POST** | `/api/user/transfer` | Перевод средств между своими картами |

---

## ⚙️ Запуск

### 🐳 Через Docker