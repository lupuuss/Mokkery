plugins {
    alias(libs.plugins.poko)
    id("mokkery-multiplatform")
    id("mokkery-publish")
}

kotlin {
    explicitApi()
    optInMokkeryDelicateAndInternals()
}

dependencies {
    commonMainApi(project(":mokkery-runtime"))
    commonMainApi(libs.kotlinx.coroutines.core)
    commonMainCompileOnly(libs.kotlin.stdlib)
    commonTestImplementation(kotlin("test"))
    commonTestImplementation(libs.kotlinx.coroutines.test)
}
