# Сборка из корня репозитория: docker build -f main-service/Dockerfile .
FROM eclipse-temurin:17-jdk-jammy AS builder
WORKDIR /workspace
COPY . .
RUN chmod +x ./gradlew && ./gradlew :main-service:bootJar --no-daemon -q

FROM eclipse-temurin:17-jre-jammy
WORKDIR /app
COPY --from=builder /workspace/main-service/build/libs/blps.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
