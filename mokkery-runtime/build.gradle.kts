plugins {
    alias(libs.plugins.poko)
    alias(libs.plugins.kotlinx.atomicfu)
    id("mokkery-multiplatform")
    id("mokkery-publish")
}

kotlin {
    explicitApi()
    optInMokkeryDelicateAndInternals()
    compilerOptions {
        freeCompilerArgs.add("-Xverify-ir=error")
    }
}

dependencies {
    kotlinCompilerPluginClasspath(project(":mokkery-plugin"))
    kotlinNativeCompilerPluginClasspath(project(":mokkery-plugin"))
    commonMainApi(project(":mokkery-core"))
    commonMainCompileOnly(libs.kotlin.stdlib)
    commonTestImplementation(kotlin("test"))
}
