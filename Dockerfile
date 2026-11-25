# Единый многоэтапный Dockerfile для всех сервисов.
# Сборка: docker build --build-arg SERVICE=booking-service --build-arg PORT=8082 -t mrb-booking-service .

# ---------- Этап 1: сборка ----------
FROM maven:3.9.6-eclipse-temurin-17 AS build
ARG SERVICE
WORKDIR /workspace
COPY . .
# Кэш локального репозитория Maven между сборками; тесты и покрытие (JaCoCo) выполняются отдельным заданием CI.
# Повторы загрузки защищают от обрывов сети при скачивании зависимостей.
RUN --mount=type=cache,target=/root/.m2 \
    mvn -B -q -Daether.connector.http.retryHandler.count=5 -Djacoco.skip=true \
        -pl "${SERVICE}" -am package -DskipTests \
    && cp "${SERVICE}"/target/*.jar /workspace/app.jar

# ---------- Этап 2: запуск ----------
FROM eclipse-temurin:17.0.10_7-jre-alpine AS runtime
ARG SERVICE
ARG PORT=8080
LABEL org.opencontainers.image.title="${SERVICE}"

# Непривилегированный пользователь без оболочки входа
RUN addgroup -S app && adduser -S -G app -H -s /sbin/nologin app
WORKDIR /app
COPY --from=build --chown=app:app /workspace/app.jar /app/app.jar
USER app

ENV SERVICE_PORT=${PORT} \
    JAVA_OPTS="-XX:MaxRAMPercentage=75 -Djava.io.tmpdir=/tmp"
EXPOSE ${PORT}

# wget входит в BusyBox, поэтому curl в образ не устанавливается
HEALTHCHECK --interval=15s --timeout=3s --start-period=60s --retries=5 \
    CMD wget -q -O /dev/null "http://localhost:${SERVICE_PORT}/actuator/health/readiness" || exit 1

ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -jar /app/app.jar"]
