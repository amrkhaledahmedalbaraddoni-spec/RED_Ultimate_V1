# syntax=docker/dockerfile:1.7
# RED backend image — source: services/backend, protocol: services/shared-proto,
# schema: data/sql (packaged into the jar, never copied into the service tree).
# Build context MUST be the repository root.
FROM gradle:8.12-jdk21 AS builder

ARG DATA_PLANE=data
WORKDIR /build

COPY services/backend/ services/backend/
COPY services/shared-proto/ services/shared-proto/
COPY ${DATA_PLANE}/sql/ ${DATA_PLANE}/sql/

WORKDIR /build/services/backend
RUN --mount=type=cache,target=/home/gradle/.gradle/caches,sharing=locked \
    --mount=type=cache,target=/home/gradle/.gradle/wrapper,sharing=locked \
    gradle clean bootJar --no-daemon

FROM eclipse-temurin:21-jre-jammy
WORKDIR /app
RUN apt-get update \
    && apt-get install -y --no-install-recommends curl \
    && rm -rf /var/lib/apt/lists/* \
    && useradd --system --uid 10001 --create-home red \
    && mkdir -p /app/logs && chown -R red:red /app
COPY --from=builder --chown=red:red /build/services/backend/build/libs/*.jar /app/app.jar
USER red
EXPOSE 8080
HEALTHCHECK --interval=30s --timeout=10s --retries=3 \
  CMD curl -f http://localhost:8080/health || exit 1
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
