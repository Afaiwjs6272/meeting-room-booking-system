# Единый многоэтапный Dockerfile для всех сервисов.
# Сборка: docker build --build-arg SERVICE=booking-service --build-arg PORT=8082 -t mrb-booking-service .

# ---------- Этап 1: сборка ----------
FROM maven:latest AS build
ARG SERVICE
WORKDIR /workspace
COPY . .

# Создаем файл настроек с зеркалом Aliyun для обхода сетевых сбоев
RUN echo '<settings><mirrors><mirror><id>aliyunmaven</id><mirrorOf>central</mirrorOf><url>https://maven.aliyun.com/repository/public</url></mirror></mirrors></settings>' > /tmp/settings.xml

# Сборка с использованием зеркала, увеличенными таймаутами и повторными попытками
RUN --mount=type=cache,target=/root/.m2 \
    mvn -B -q -s /tmp/settings.xml \
        -Daether.connector.http.retryHandler.count=10 \
        -Dmaven.wagon.http.connectionTimeout=60000 \
        -Dmaven.wagon.http.readTimeout=60000 \
        -Djacoco.skip=true \
        -pl "${SERVICE}" -am package -DskipTests \
    && cp "${SERVICE}"/target/*.jar /workspace/app.jar

# ---------- Этап 2: запуск ----------
FROM eclipse-temurin:17.0.10_7-jre-alpine AS runtime
ARG SERVICE
ARG PORT=8080
LABEL org.opencontainers.image.title="${SERVICE}"

# 1. Обновляем системные пакеты Alpine (исправляет CVE в libcrypto3, libexpat, musl и т.д.)
# 2. Создаем непривилегированного пользователя без оболочки входа
# 3. Удаляем кэш apk, чтобы уменьшить размер финального образа
RUN apk update && apk upgrade --no-cache \
    && addgroup -S app && adduser -S -G app -H -s /sbin/nologin app \
    && rm -rf /var/cache/apk/*

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
