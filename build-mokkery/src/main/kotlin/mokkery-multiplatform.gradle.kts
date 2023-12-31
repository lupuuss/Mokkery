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

    applyDefaultHierarchyTemplate()

    iosX64()
    iosArm64()
    iosSimulatorArm64()

    tvosX64()
    tvosArm64()
    tvosSimulatorArm64()

    watchosX64()
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

    sourceSets {
        all {
            languageSettings.optIn("dev.mokkery.annotations.DelicateMokkeryApi")
            languageSettings.optIn("dev.mokkery.annotations.InternalMokkeryApi")
        }

        val blockingMain by creating {
            dependsOn(commonMain.get())
        }

        jvmMain { dependsOn(blockingMain) }
        nativeMain { dependsOn(blockingMain) }
    }
}
