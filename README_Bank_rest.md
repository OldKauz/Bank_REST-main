# Bank REST API

Учебный проект для управления банковскими картами.

## Стек
- Java 17+
- Spring Boot 3
- Spring Security (JWT)
- Spring Data JPA
- Liquibase
- PostgreSQL
- Docker Compose
- Swagger / OpenAPI

## Запуск
1. Собрать JAR-файл:
   ```bash
   mvn clean package -DskipTests

2. Запустить весь стек:
   ```bash
   docker-compose up --build

3. Приложение будет доступно по адресу:
- API: http://localhost:8080/api
- Swagger UI: http://localhost:8080/swagger-ui.html
4. PostgreSQL:
- Хост: localhost
- Порт: 5555
- Пользователь: bank_user
- Пароль: bank_password
- База: bank_db