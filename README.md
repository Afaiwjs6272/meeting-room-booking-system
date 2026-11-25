# Meeting Room Booking System

Микросервисная система бронирования переговорных комнат: `api-gateway`, `reference-service`, `booking-service`,
`notification-service`, `integration-service`, общий модуль `common`; обмен событиями через Kafka, у каждого
сервиса своя база PostgreSQL.

## Запуск одной командой (Docker)

```bash
cp .env.example .env          # задайте пароли в .env
docker compose up -d --build --wait
curl http://localhost:8080/actuator/health
```

Наружу публикуется только `api-gateway` (порт 8080). PostgreSQL и Kafka доступны только внутри сети Compose
(Kafka для отладки — на `127.0.0.1:9092`).

| Сервис | Порт внутри сети | Назначение |
|---|---|---|
| api-gateway | 8080 | единая точка входа |
| reference-service | 8081 | офисы, комнаты, сотрудники |
| booking-service | 8082 | бронирования, напоминания, события Kafka |
| notification-service | 8083 | уведомления (email, Telegram) |
| integration-service | 8084 | синхронизация с LDAP и 1С |

Примеры запросов через шлюз (демонстрационные данные создаются миграцией `reference-service/V2__demo_data.sql`):

```bash
curl http://localhost:8080/api/v1/rooms/a0000000-0000-0000-0000-000000000001
curl -X POST http://localhost:8080/api/v1/bookings -H 'Content-Type: application/json' -d '{
  "roomId": "a0000000-0000-0000-0000-000000000001",
  "userId": "e0000000-0000-0000-0000-000000000002",
  "title": "Планирование", "startTime": "2030-01-10T10:00:00", "endTime": "2030-01-10T11:00:00"}'
```

## Мониторинг

```bash
docker compose --profile monitoring up -d
```

Prometheus: <http://localhost:9090>, Grafana: <http://localhost:3000> (пользователь `admin`, пароль из `.env`),
дашборд «Meeting Room Booking — обзор» подключается автоматически. Метрики сервисов:
`/actuator/prometheus`, проверки состояния: `/actuator/health/liveness` и `/actuator/health/readiness`.

## Тесты и покрытие

```bash
mvn -B verify          # модульные и интеграционные тесты (Testcontainers требует Docker) + JaCoCo
```

Отчёт покрытия: `<модуль>/target/site/jacoco/index.html`. Порог покрытия строк `booking-service`
задаётся свойством `coverage.minimum` в его `pom.xml`.

## CI

`.github/workflows/ci.yml`: hadolint → сборка и тесты → Trivy (зависимости, секреты, конфигурация) → образы
всех сервисов (сборка, Trivy, публикация в GHCR из ветки `main`) → дымовой тест всей системы.

## Нагрузочный тест и измерения

```bash
k6 run -e BASE_URL=http://localhost:8080 loadtest/booking.js     # цель: p95 < 300 мс при 100 пользователях
scripts/measure.sh images     # размеры образов и время сборки (таблица размеров)
scripts/measure.sh tests      # число тестов и покрытие
LABEL=before scripts/measure.sh trivy    # уязвимости до исправлений (затем LABEL=after)
LABEL=before scripts/measure.sh bench    # docker-bench-security
```

Для чистых измерений перед нагрузочным тестом перезапустите систему с очисткой данных: `docker compose down -v`.

## Секреты

Пароли и учётные данные хранятся только в `.env` (в git не попадает) или в секретах CI. В `application.yml`
реальных паролей нет: почтовые учётные данные читаются из `MAIL_USERNAME` и `MAIL_PASSWORD`.
