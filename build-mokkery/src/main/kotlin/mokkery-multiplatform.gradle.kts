@file:Suppress("UNUSED_VARIABLE")

plugins {
    id("mokkery-publish")
    kotlin("multiplatform")
}

kotlin {
    explicitApi()
    jvm()
    js(IR) {
        browser()
        nodejs()
    }

    ios()
    iosSimulatorArm64()

    tvos()
    tvosSimulatorArm64()

    watchos()
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

    sourceSets {
        all {
            languageSettings.optIn("dev.mokkery.annotations.DelicateMokkeryApi")
            languageSettings.optIn("dev.mokkery.annotations.InternalMokkeryApi")
        }

        val commonMain by getting

        val darwinMain by creating
        val androidNativeMain by creating

        val iosMain by getting { dependsOn(darwinMain) }
        val iosSimulatorArm64Main by getting { dependsOn(iosMain) }

        val watchosMain by getting { dependsOn(darwinMain) }
        val watchosSimulatorArm64Main by getting { dependsOn(darwinMain) }
        val watchosDeviceArm64Main by getting { dependsOn(darwinMain) }

        val tvosMain by getting { dependsOn(darwinMain) }
        val tvosSimulatorArm64Main by getting { dependsOn(darwinMain) }

        val macosX64Main by getting { dependsOn(darwinMain) }
        val macosArm64Main by getting { dependsOn(darwinMain) }

        val jvmMain by getting

        val mingwX64Main by getting
        val linuxX64Main by getting
        val linuxArm64Main by getting

        val androidNativeX64Main by getting { dependsOn(androidNativeMain) }
        val androidNativeX86Main by getting { dependsOn(androidNativeMain) }
        val androidNativeArm32Main by getting { dependsOn(androidNativeMain) }
        val androidNativeArm64Main by getting { dependsOn(androidNativeMain) }

        val blockingMain by creating {
            dependsOn(commonMain)
            listOf(
                jvmMain,
                mingwX64Main,
                linuxX64Main,
                linuxArm64Main,
                darwinMain,
                androidNativeMain
            ).forEach {
                it.dependsOn(this)
            }
        }
    }
}
