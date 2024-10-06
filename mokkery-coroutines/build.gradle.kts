plugins {
    id("mokkery-publish")
    id("mokkery-multiplatform")
    alias(libs.plugins.poko)
}

dependencies {
    commonMainApi(project(":mokkery-runtime"))
    commonMainApi(libs.kotlinx.coroutines.core)

    commonTestImplementation(kotlin("test"))
    commonTestImplementation(libs.kotlinx.coroutines.test)
}
