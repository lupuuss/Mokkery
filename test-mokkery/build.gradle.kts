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
    "ignoreFinalMembers" to "true",
    "stubs.allowConcreteClassInstantiation" to "true",
    "stubs.allowClassInheritance" to "true",
    "annotations.copyToMock" to """all - named("dev.mokkery.test.AnnotationB"|"dev.mokkery.test.AnnotationC")"""
)

dependencies {
    kotlinCompilerPluginClasspath(project(":mokkery-plugin"))
    kotlinNativeCompilerPluginClasspath(project(":mokkery-plugin"))
    commonMainImplementation(project(":mokkery-runtime"))
    commonMainImplementation(project(":mokkery-coroutines"))
    commonTestImplementation(kotlin("test"))
    commonTestImplementation(libs.kotlinx.coroutines.test)
}
