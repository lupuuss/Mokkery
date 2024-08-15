plugins {
    id("kotlinx-atomicfu")
    id("mokkery-publish")
    id("mokkery-multiplatform")
    alias(libs.plugins.poko)
}

dependencies {
    commonMainApi(project(":mokkery-core"))
    commonTestImplementation(kotlin("test"))
}
