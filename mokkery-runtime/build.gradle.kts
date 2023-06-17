plugins {
    alias(libs.plugins.kotlin.multiplatform)
    `maven-publish`
    id("kotlinx-atomicfu")
}

kotlin {
    explicitApi()
    jvm()
    js(IR) {}
    mingwX64()

    sourceSets {
        all {
            languageSettings.optIn("dev.mokkery.annotations.DelicateMokkeryApi")
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

dependencies {
    commonMainImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.1")
}
