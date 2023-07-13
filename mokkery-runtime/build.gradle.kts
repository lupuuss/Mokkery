plugins {
    id("kotlinx-atomicfu")
    id("mokkery-multiplatform")
}

dependencies {
    commonMainApi(project(":mokkery-core"))
    commonMainImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.1")
    commonTestImplementation(kotlin("test"))
}
