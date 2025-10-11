plugins {
    id("mokkery-publish")
    id("mokkery-multiplatform")
    alias(libs.plugins.poko)
}

kotlin.explicitApi()

dependencies {
    commonMainApi(project(":mokkery-runtime"))
    commonMainApi(libs.kotlinx.coroutines.core)
    commonMainCompileOnly(libs.kotlin.stdlib)

    jsMainCompileOnly(libs.kotlin.dom.api.compat)

    commonTestImplementation(kotlin("test"))
    commonTestImplementation(libs.kotlinx.coroutines.test)
}
