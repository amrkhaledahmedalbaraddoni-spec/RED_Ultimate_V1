plugins {
    kotlin("jvm") version "2.1.0"
    kotlin("plugin.spring") version "3.4.0"
    id("org.springframework.boot") version "3.4.0"
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-websocket")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-data-mongodb")
    implementation("org.springframework.boot:spring-boot-starter-data-redis")
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.flywaydb:flyway-core")
    implementation("org.flywaydb:flyway-database-postgresql")
    implementation("org.postgresql:postgresql")
    implementation("org.asteriskjava:asterisk-java:3.40.0") // System B Engine
    implementation("com.google.protobuf:protobuf-java:3.25.1")
}
