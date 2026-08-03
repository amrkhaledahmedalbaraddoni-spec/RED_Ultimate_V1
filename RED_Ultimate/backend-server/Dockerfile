# RED Sovereign Backend - Production Dockerfile
FROM gradle:8.12-jdk21 AS builder

WORKDIR /build
COPY build.gradle.kts settings.gradle.kts ./
COPY src/ src/

# Build without tests for speed
RUN gradle build -x test --no-daemon

# Runtime image
FROM eclipse-temurin:21-jre-jammy

WORKDIR /app
COPY --from=builder /build/build/libs/*.jar app.jar

EXPOSE 8080

# Health check using Spring Boot actuator (if configured) or simple TCP check
HEALTHCHECK --interval=30s --timeout=10s --retries=3 \
  CMD curl -f http://localhost:8080/health || exit 1

ENTRYPOINT ["java", "-jar", "app.jar"]
