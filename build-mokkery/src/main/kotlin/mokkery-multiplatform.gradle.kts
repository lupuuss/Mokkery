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
