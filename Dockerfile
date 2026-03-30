# ── Stage 1: build ────────────────────────────────────────
FROM eclipse-temurin:21-jdk-alpine AS build
WORKDIR /app

# Copy Gradle wrapper + config first (layer-cache friendly)
COPY gradle gradle
COPY gradlew gradlew.bat build.gradle settings.gradle ./
RUN chmod +x gradlew && ./gradlew dependencies --no-daemon -q

# Copy source and build the fat JAR
COPY src src
RUN ./gradlew bootJar --no-daemon -q

# ── Stage 2: run ──────────────────────────────────────────
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=build /app/build/libs/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
