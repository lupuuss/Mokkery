plugins {
    id("mokkery-coroutines") // TODO replace and remove when all targets being supported by Kotlinx Coroutines
    alias(libs.plugins.poko)
}

dependencies {
    commonMainApi(project(":mokkery-runtime"))
    commonMainApi(libs.kotlinx.coroutines.core)

    commonTestImplementation(kotlin("test"))
    commonTestImplementation(libs.kotlinx.coroutines.test)
}
