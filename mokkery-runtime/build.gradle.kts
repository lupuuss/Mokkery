plugins {
    id("kotlinx-atomicfu")
    id("mokkery-multiplatform")
    id("org.jetbrains.dokka")
}

dependencies {
    commonMainApi(project(":mokkery-core"))
    commonMainImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.1")
}
