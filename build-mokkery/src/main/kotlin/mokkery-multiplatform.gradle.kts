plugins {
    id("mokkery-base")
    kotlin("multiplatform")
}

kotlin {
    explicitApi()
    jvm()
    js(IR) {}

    ios()
    iosSimulatorArm64()

    tvos()
    tvosSimulatorArm64()

    watchos()

    macosArm64()
    macosX64()

    mingwX64()
    linuxX64()

    sourceSets {
        all {
            languageSettings.optIn("dev.mokkery.annotations.DelicateMokkeryApi")
            languageSettings.optIn("dev.mokkery.annotations.InternalMokkeryApi")
        }

        val commonMain by getting

        val darwinMain by creating

        val iosMain by getting { dependsOn(darwinMain) }
        val iosSimulatorArm64Main by getting { dependsOn(iosMain) }

        val watchosMain by getting { dependsOn(darwinMain) }

        val tvosMain by getting { dependsOn(darwinMain) }
        val tvosSimulatorArm64Main by getting { dependsOn(darwinMain) }

        val macosX64Main by getting { dependsOn(darwinMain) }
        val macosArm64Main by getting { dependsOn(darwinMain) }

        val jvmMain by getting

        val mingwX64Main by getting
        val linuxX64Main by getting

        val blockingMain by creating {
            dependsOn(commonMain)
            jvmMain.dependsOn(this)
            mingwX64Main.dependsOn(this)
            linuxX64Main.dependsOn(this)
            darwinMain.dependsOn(this)
        }
    }
}
