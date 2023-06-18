plugins {
    id("mokkery-base")
    kotlin("multiplatform")
}

kotlin {
    explicitApi()
    jvm()
    js(IR) {}
    mingwX64()

    sourceSets {
        all {
            languageSettings.optIn("dev.mokkery.annotations.DelicateMokkeryApi")
            languageSettings.optIn("dev.mokkery.annotations.InternalMokkeryApi")
        }

        val commonMain by getting
        val jvmMain by getting
        val mingwX64Main by getting
        val blockingMain by creating {
            dependsOn(commonMain)
            jvmMain.dependsOn(this)
            mingwX64Main.dependsOn(this)
        }
    }
}
