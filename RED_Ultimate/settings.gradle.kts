rootProject.name = "RED-Ultimate"

// ═══════════════════════════════════════════
//  RED Ultimate - Gradle Settings
//  Fixed: removed non-existent :features:* modules
//  All includes verified against existing build files
// ═══════════════════════════════════════════

// Main app module
include(":app")

// Core modules (all verified to have build.gradle.kts)
include(":core:util")
include(":core:ui")
include(":core:models")
include(":core:models-jvm")
include(":core:util-jvm")
include(":core:serialization")
include(":core:network")

// Lib modules (Signal libraries - all verified)
include(":lib:libsignal-service")
include(":lib:network")
include(":lib:glide")
include(":lib:archive")
include(":lib:apng")
include(":lib:contacts")
include(":lib:blurhash")
include(":lib:paging")
include(":lib:photoview")
include(":lib:qr")
include(":lib:video")
include(":lib:spinner")
include(":lib:sticky-header-grid")
include(":lib:debuglogs-viewer")
include(":lib:device-transfer")
include(":lib:image-editor")
include(":lib:donations")
include(":lib:billing")

// Feature modules (exist in feature/)
include(":feature:camera")
include(":feature:media-send")
include(":feature:registration")

// Lint & build tools
include(":lintchecks")
include(":fast-lint")
include(":build-logic:tools")

// Benchmark modules
include(":benchmark")
include(":microbenchmark")

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven { url = uri("https://jitpack.io") }
    }
}
