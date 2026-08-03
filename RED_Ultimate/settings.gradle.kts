rootProject.name = "RED-Ultimate"

include(":app")
include(":core")
include(":lib:libsignal-service")
include(":lib:network")
include(":features:chat")
include(":features:calls")
include(":features:pstn")
include(":features:stories")
include(":features:auth")
include(":features:profile")

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven { url = uri("https://jitpack.io") }
    }
}
