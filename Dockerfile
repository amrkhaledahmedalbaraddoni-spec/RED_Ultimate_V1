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
    && ./gradlew :app:dependencies --write-verification-metadata sha256 --no-configuration-cache --no-daemon > /tmp/android-metadata.log 2>&1 \
    && echo '=== RED_ADDITIONAL_VERIFICATION_CHECKSUMS ===' \
    && python3 -c 'import xml.etree.ElementTree as E; r=E.parse("gradle/verification-metadata.xml").getroot(); ns={"v":"https://schema.gradle.org/dependency-verification"}; wanted={("androidx.emoji2","emoji2","1.2.0"),("org.signal","libsignal-android","0.86.5"),("org.signal","libsignal-client","0.86.5"),("com.google.protobuf","protobuf-java","3.25.1"),("androidx.savedstate","savedstate-android","1.3.0")}; [print("VERIFY|"+c.attrib["group"]+"|"+c.attrib["name"]+"|"+c.attrib["version"]+"|"+a.attrib["name"]+"|"+s.attrib["value"]) for c in r.findall(".//v:component",ns) if (c.attrib["group"],c.attrib["name"],c.attrib["version"]) in wanted for a in c.findall("v:artifact",ns) for s in a.findall("v:sha256",ns)]' \
    && ./gradlew :app:assembleDebug -PRED_SERVER_URL=http://127.0.0.1 --dependency-verification strict --no-configuration-cache --no-daemon > /tmp/android-build.log 2>&1 \
    || (echo '=== RED_ANDROID_GRADLE_FAILURE ==='; tail -n 160 /tmp/android-build.log; exit 1)

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
