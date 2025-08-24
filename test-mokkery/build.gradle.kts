@file:OptIn(ExperimentalKotlinGradlePluginApi::class, ExperimentalWasmDsl::class)

import dev.mokkery.gradle.mokkery
import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl

buildscript {
    dependencies {
        classpath("com.android.tools.build:gradle:8.11.0")
    }
}

plugins {
    kotlin("multiplatform")
    id("dev.mokkery")
    id("org.jetbrains.kotlin.plugin.allopen")
    id("com.android.library") version "8.11.0"
}

android {
    namespace = "dev.mokkery.test"
    compileSdk = 36
}

allOpen {
    annotation("dev.mokkery.test.OpenForMokkery")
}

mokkery {
    ignoreFinalMembers.set(true)
}

kotlin {

    applyDefaultHierarchyTemplate {
        common {
            group("wasm") {
                withWasmJs()
                withWasmWasi()
            }
        }
    }

    androidTarget()

    jvm()

    js(IR) {
        browser()
        nodejs()
    }

    wasmJs {
        browser()
        nodejs()
    }
    wasmWasi {
        nodejs()
    }

    iosX64()
    iosArm64()
    iosSimulatorArm64()

    tvosX64()
    tvosArm64()
    tvosSimulatorArm64()

    watchosX64()
    watchosArm32()
    watchosArm64()
    watchosSimulatorArm64()
    watchosDeviceArm64()

    macosArm64()
    macosX64()

    mingwX64()

    linuxX64()
    linuxArm64()

    androidNativeArm32()
    androidNativeArm64()

    androidNativeX86()
    androidNativeX64()

    @OptIn(ExperimentalKotlinGradlePluginApi::class)
    compilerOptions {
        freeCompilerArgs.add("-Xexpect-actual-classes")
    }
}

dependencies {
    commonTestImplementation(kotlin("test"))
    commonTestImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.9.0")
    commonTestImplementation(mokkery("coroutines"))
}
