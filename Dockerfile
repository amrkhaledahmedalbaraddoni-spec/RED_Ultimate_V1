# Root CI image: compile and test the canonical RED backend with the shared protocol.
FROM gradle:8.12-jdk21 AS builder
WORKDIR /build
COPY RED_Ultimate/backend-server/ backend-server/
COPY RED_Ultimate/shared-proto/ shared-proto/
WORKDIR /build/backend-server
RUN gradle clean build --no-daemon > /tmp/gradle-build.log 2>&1 \
    || (echo '=== RED_BACKEND_GRADLE_FAILURE ==='; tail -n 120 /tmp/gradle-build.log; exit 1)

FROM ghcr.io/cirruslabs/android-sdk:35 AS android-builder
WORKDIR /build/RED_Ultimate
COPY RED_Ultimate/build.gradle.kts RED_Ultimate/settings.gradle.kts RED_Ultimate/gradle.properties RED_Ultimate/gradlew ./
COPY RED_Ultimate/gradle/ gradle/
COPY RED_Ultimate/build-logic/ build-logic/
COPY RED_Ultimate/wire-handler/ wire-handler/
COPY RED_Ultimate/red-app/ red-app/
COPY RED_Ultimate/shared-proto/ shared-proto/
RUN chmod +x gradlew \
    && ./gradlew :app:assembleDebug -PRED_SERVER_URL=http://127.0.0.1 --write-verification-metadata sha256 --no-daemon > /tmp/android-build.log 2>&1 \
    || (echo '=== RED_ANDROID_GRADLE_FAILURE ==='; tail -n 160 /tmp/android-build.log; exit 1)
RUN echo '=== RED_GENERATED_VERIFICATION_CHECKSUMS ===' \
    && awk '/<component / { component=$0; wanted=($0 ~ /hilt-android-gradle-plugin.*2.52/ || $0 ~ /kotlin-serialization.*2.2.21/ || $0 ~ /slf4j-api.*1.7.30/) } wanted && /<artifact / { artifact=$0 } wanted && /<sha256 / { print "VERIFY|" component "|" artifact "|" $0 }' gradle/verification-metadata.xml

FROM eclipse-temurin:21-jre-jammy
WORKDIR /app
RUN apt-get update \
    && apt-get install -y --no-install-recommends curl \
    && rm -rf /var/lib/apt/lists/*
COPY --from=builder /build/backend-server/build/libs/*.jar app.jar
COPY --from=android-builder /build/RED_Ultimate/red-app/build/outputs/apk/debug/*.apk /opt/red-app-debug.apk
EXPOSE 8080
HEALTHCHECK --interval=30s --timeout=10s --retries=3 CMD curl -f http://localhost:8080/health || exit 1
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
