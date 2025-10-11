import org.jetbrains.kotlin.gradle.plugin.SubpluginOption

plugins {
    id("mokkery-multiplatform")
    alias(libs.plugins.agp.library)
    alias(libs.plugins.kotlin.allopen)
}

kotlin {
    androidTarget()
}

android {
    namespace = "dev.mokkery.test"
    compileSdk = 36
}

allOpen {
    annotation("dev.mokkery.test.OpenForMokkery")
}

configureCompilerPlugin(
    "dev.mokkery",
    SubpluginOption("ignoreFinalMembers", "true")
)

dependencies {
    kotlinCompilerPluginClasspath(project(":mokkery-plugin"))
    kotlinNativeCompilerPluginClasspath(project(":mokkery-plugin"))
    kotlinNativeCompilerPluginClasspath(project(":mokkery-core"))
    commonMainImplementation(project(":mokkery-runtime"))
    commonMainImplementation(project(":mokkery-coroutines"))
    commonTestImplementation(kotlin("test"))
    commonTestImplementation(libs.kotlinx.coroutines.test)
}
