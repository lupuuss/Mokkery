import dev.mokkery.gradle.mokkery
import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.plugin.kotlinToolingVersion
import org.jetbrains.kotlin.gradle.targets.js.dsl.ExperimentalWasmDsl

plugins {
    kotlin("multiplatform")
    id("dev.mokkery")
    id("org.jetbrains.kotlin.plugin.allopen")
}

allOpen {
    annotation("dev.mokkery.test.OpenForMokkery")
}

val mokkeryAllowIndirectSuperCalls: String by project

mokkery {
    allowIndirectSuperCalls.set(mokkeryAllowIndirectSuperCalls.toBoolean())
    ignoreFinalMembers.set(true)
}

kotlin {

    jvm()

    js(IR) {
        browser()
        nodejs()
    }

    @OptIn(ExperimentalWasmDsl::class)
    wasmJs {
        browser()
        nodejs()
    }
    @OptIn(ExperimentalWasmDsl::class)
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

if (kotlinToolingVersion.toString() == "2.0.0") {
    configurations
        .filter { it.name.startsWith("wasm") }
        .forEach {
            it.resolutionStrategy.force("org.jetbrains.kotlin:kotlin-stdlib:2.0.0")
        }
}
