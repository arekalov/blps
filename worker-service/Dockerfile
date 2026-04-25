# Сборка из корня репозитория: docker build -f worker-service/Dockerfile .
FROM eclipse-temurin:17-jdk-jammy AS builder
WORKDIR /workspace
COPY . .
RUN chmod +x ./gradlew && ./gradlew :worker-service:bootJar --no-daemon -q

FROM eclipse-temurin:17-jre-jammy
WORKDIR /app
COPY --from=builder /workspace/worker-service/build/libs/blps-worker.jar app.jar
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
