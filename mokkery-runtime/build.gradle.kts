plugins {
    id("org.jetbrains.kotlinx.atomicfu")
    id("mokkery-multiplatform")
    alias(libs.plugins.poko)
}

dependencies {
    commonMainApi(project(":mokkery-core"))
    commonTestImplementation(kotlin("test"))
    commonMainImplementation(libs.kotlinx.coroutines.core)
    commonTestImplementation(libs.kotlinx.coroutines.test)
}
