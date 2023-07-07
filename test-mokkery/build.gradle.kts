plugins {
    kotlin("multiplatform")
    id("dev.mokkery.plugin")
}

kotlin {

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

    macosArm64()
    macosX64()

    mingwX64()
    linuxX64()

    sourceSets {

        val commonTest by getting {
            dependencies {
                implementation(kotlin("test"))
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.7.1")
            }
        }
    }
}
