@file:Suppress("UnstableApiUsage")

rootProject.name = "mokkery"

pluginManagement {
    repositories {
        gradlePluginPortal()
        google()
        mavenCentral()
        maven("https://packages.jetbrains.team/maven/p/compiler-plugin-dev-kit/eap")
    }
}

plugins {
    id("org.gradle.toolchains.foojay-resolver") version "1.0.0"
    id("org.jetbrains.kotlin.compiler.plugin.devkit") version "0.0.4-dev-402"
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
        maven("https://packages.jetbrains.team/maven/p/compiler-plugin-dev-kit/eap")
    }
}
includeBuild("build-mokkery")
include(":mokkery-core")
include(":mokkery-core-plugin")
include(":mokkery-core-tooling")
include(":mokkery-gradle")
include(":mokkery-plugin")
include(":mokkery-runtime")
include(":mokkery-coroutines")
include(":test-mokkery")
include(":test-mokkery-gradle")
include(":test-mokkery-performance")
