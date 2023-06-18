rootProject.name = "mokkery"

pluginManagement {
    repositories {
        gradlePluginPortal()
        google()
        mavenCentral()
    }
}

dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
    }
}
includeBuild("build-mokkery")
include(":mokkery-core")
include(":mokkery-gradle")
include(":mokkery-plugin")
include(":mokkery-runtime")
