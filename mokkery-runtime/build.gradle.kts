plugins {
    id("mokkery-publish")
    id("mokkery-multiplatform")
    alias(libs.plugins.poko)
    alias(libs.plugins.kotlinx.atomicfu)
}

dependencies {
    commonMainApi(project(":mokkery-core"))
    commonTestImplementation(kotlin("test"))
    jvmMainImplementation(libs.objenesis)
    jvmMainImplementation(libs.bytebuddy)
}
