@file:Suppress("UnstableApiUsage")

rootProject.name = "mokkery"

pluginManagement {
    repositories {
        gradlePluginPortal()
        google()
        mavenCentral()
    }
}

plugins {
    id("org.gradle.toolchains.foojay-resolver") version "0.8.0"
}

toolchainManagement {
    jvm {
        javaRepositories {
            repository("foojay") {
                resolverClass.set(org.gradle.toolchains.foojay.FoojayToolchainResolver::class.java)
            }
        }
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
include(":mokkery-coroutines")
include(":test-mokkery")

