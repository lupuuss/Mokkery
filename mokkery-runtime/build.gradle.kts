
plugins {
    id("mokkery-publish")
    id("mokkery-multiplatform")
    alias(libs.plugins.poko)
    alias(libs.plugins.kotlinx.atomicfu)
}

kotlin.compilerOptions.freeCompilerArgs.add("-Xcontext-parameters")

dependencies {
    kotlinCompilerPluginClasspath(project(":mokkery-plugin"))
    kotlinNativeCompilerPluginClasspath(project(":mokkery-plugin"))
    kotlinNativeCompilerPluginClasspath(project(":mokkery-core"))
    commonMainApi(project(":mokkery-core"))
    commonTestImplementation(kotlin("test"))
    jvmMainImplementation(libs.objenesis)
    jvmMainImplementation(libs.bytebuddy)
}
