plugins {
    id("mokkery-publish")
    id("mokkery-multiplatform")
    alias(libs.plugins.poko)
    alias(libs.plugins.kotlinx.atomicfu)
}

kotlin {
    explicitApi()
    optInMokkeryDelicateAndInternals()
}

dependencies {
    kotlinCompilerPluginClasspath(project(":mokkery-plugin"))
    kotlinNativeCompilerPluginClasspath(project(":mokkery-plugin"))
    kotlinNativeCompilerPluginClasspath(project(":mokkery-core"))
    kotlinNativeCompilerPluginClasspath(project(":mokkery-core-tooling"))
    commonMainApi(project(":mokkery-core"))
    commonMainCompileOnly(libs.kotlin.stdlib)

    jsMainCompileOnly(libs.kotlin.dom.api.compat)

    commonTestImplementation(kotlin("test"))
}
