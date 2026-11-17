@file:Suppress("UNUSED_VARIABLE")

import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.targets.wasm.wasmtime.WasmtimeEnvSpec
import org.jetbrains.kotlin.gradle.targets.wasm.wasmtime.WasmtimePlugin


plugins {
    kotlin("multiplatform")
    id("mokkery-base")
}

kotlin {

    @OptIn(ExperimentalKotlinGradlePluginApi::class)
    applyDefaultHierarchyTemplate {
        common {
            group("wasm") {
                withWasmJs()
                withWasmWasi()
            }
        }
    }

    jvm()

    js {
        browser()
        nodejs()
    }
    wasmJs {
        browser()
        nodejs()
    }
    @OptIn(ExperimentalWasmDsl::class)
    wasmWasi {
        wasmtime()
    }

    iosX64()
    iosArm64()
    iosSimulatorArm64()

    tvosArm64()
    tvosSimulatorArm64()

    watchosArm64()
    watchosSimulatorArm64()
    watchosDeviceArm64()

    macosArm64()

    mingwX64()

    linuxX64()
    linuxArm64()

    androidNativeArm32()
    androidNativeArm64()

    androidNativeX86()
    androidNativeX64()

    compilerOptions {
        freeCompilerArgs.add("-Xexpect-actual-classes")
    }
}

@OptIn(ExperimentalWasmDsl::class)
plugins.withType<WasmtimePlugin> {
    the<WasmtimeEnvSpec>().version.set("50.0.1")
}
