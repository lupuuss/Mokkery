plugins {
    id("kotlinx-atomicfu")
    id("mokkery-multiplatform")
    alias(libs.plugins.poko)
}

dependencies {
    commonMainApi(project(":mokkery-core"))
    commonTestImplementation(kotlin("test"))

    coroutinesMainImplementation(libs.kotlinx.coroutines.core)
    coroutinesTestImplementation(libs.kotlinx.coroutines.test)
}
