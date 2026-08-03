# RED Sovereign Backend - Ultimate Production Dockerfile V2
# Multi-stage build optimized for Spring Boot 3.4 + Kotlin + Java 21

FROM gradle:8.10-jdk21 AS builder

WORKDIR /build

# Copy gradle config first for layer caching
COPY build.gradle.kts settings.gradle.kts ./
COPY gradle.properties . 2>/dev/null || echo "org.gradle.jvmargs=-Xmx2g" > gradle.properties

# Copy source
COPY src/ src/

# Build - Skip tests for speed, but compile all
RUN gradle build -x test --no-daemon --parallel -Dorg.gradle.caching=true || \
    gradle build -x test --no-daemon --stacktrace

# Runtime - Distroless-like but with curl for healthcheck
FROM eclipse-temurin:21-jre-jammy

LABEL maintainer="RED Sovereign Engineering © 2026"
LABEL version="2.0.0-ULTIMATE"
LABEL system="RED Ultimate Sovereign - 100% Local"

WORKDIR /app

# Install curl for healthchecks + timezone data
RUN apt-get update && apt-get install -y curl tzdata && rm -rf /var/lib/apt/lists/*

# Copy jar
COPY --from=builder /build/build/libs/*.jar app.jar

# Create logs directory
RUN mkdir -p /app/logs && chmod 755 /app/logs

EXPOSE 8080

# Health check
HEALTHCHECK --interval=20s --timeout=10s --retries=5 --start-period=60s \
  CMD curl -f http://localhost:8080/health || curl -f http://localhost:8080/actuator/health || exit 1

# JVM tuning for sovereign server
ENV JAVA_OPTS="-Xms512m -Xmx2g -XX:+UseG1GC -XX:+UseStringDeduplication -Djava.security.egd=file:/dev/./urandom"

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
