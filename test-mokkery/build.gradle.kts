import org.jetbrains.kotlin.gradle.plugin.SubpluginOption

plugins {
    id("mokkery-multiplatform")
    alias(libs.plugins.kotlin.allopen)
}

allOpen {
    annotation("dev.mokkery.test.OpenForMokkery")
}

configureCompilerPlugin(
    "dev.mokkery",
    SubpluginOption("ignoreFinalMembers", "true"),
    SubpluginOption("stubs.allowConcreteClassInstantiation", "true"),
    SubpluginOption("stubs.allowClassInheritance", "true"),
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
