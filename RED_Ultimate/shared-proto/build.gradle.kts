plugins {
    id("java-library")
    id("com.google.protobuf") version "0.9.4"
}

java {
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
}

dependencies {
    implementation("com.google.protobuf:protobuf-java:3.25.1")
}

sourceSets {
    main {
        proto {
            srcDir(project.projectDir)
            include("*.proto")
        }
    }
}

protobuf {
    protoc {
        artifact = "com.google.protobuf:protoc:3.25.1"
    }
}
