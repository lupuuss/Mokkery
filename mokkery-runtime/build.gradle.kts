plugins {
    id("kotlinx-atomicfu")
    id("mokkery-multiplatform")
}

dependencies {
    commonMainApi(project(":mokkery-core"))
    commonMainImplementation(libs.kotlinx.coroutines.core)
    commonTestImplementation(kotlin("test"))
    commonTestImplementation(libs.kotlinx.coroutines.test)
}
