# Root CI image: compile and test the canonical RED backend with the shared protocol.
FROM gradle:9.2.1-jdk21 AS builder
WORKDIR /build
COPY RED_Ultimate/backend-server/ backend-server/
COPY RED_Ultimate/shared-proto/ shared-proto/
WORKDIR /build/backend-server
RUN gradle clean build --no-daemon > /tmp/gradle-build.log 2>&1 \
    || (grep -E "(^e: |^.*error: |FAILURE:|What went wrong|Could not|Execution failed|Unresolved reference)" /tmp/gradle-build.log | tail -n 160; exit 1)

FROM eclipse-temurin:21-jre-jammy
WORKDIR /app
RUN apt-get update \
    && apt-get install -y --no-install-recommends curl \
    && rm -rf /var/lib/apt/lists/*
COPY --from=builder /build/backend-server/build/libs/*.jar app.jar
EXPOSE 8080
HEALTHCHECK --interval=30s --timeout=10s --retries=3 CMD curl -f http://localhost:8080/health || exit 1
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
