plugins {
    id("mokkery-publish")
    id("mokkery-multiplatform")
    alias(libs.plugins.poko)
    alias(libs.plugins.kotlinx.atomicfu)
}

dependencies {
    commonMainApi(project(":mokkery-core"))
    commonMainCompileOnly(libs.kotlin.stdlib)

    jsMainCompileOnly(libs.kotlin.dom.api.compat)

    jvmMainImplementation(libs.objenesis)
    jvmMainImplementation(libs.bytebuddy)

    commonTestImplementation(kotlin("test"))
}
