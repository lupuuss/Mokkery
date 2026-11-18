import org.jetbrains.kotlin.gradle.targets.js.ir.KotlinJsIrLink
import org.jetbrains.kotlin.gradle.tasks.KotlinNativeLink

plugins {
    id("mokkery-multiplatform")
    alias(libs.plugins.kotlin.allopen)
}

allOpen {
    annotation("dev.mokkery.test.OpenForMokkery")
}

configureCompilerPlugin(
    MokkeryAttributes.PluginId,
    "ignoreFinalMembers" to "true",
    "stubs.allowConcreteClassInstantiation" to "true",
    "stubs.allowClassInheritance" to "true",
    "annotations.copyToMock" to """all - named("dev.mokkery.test.AnnotationB"|"dev.mokkery.test.AnnotationC")"""
)

kotlin {
    providers.gradleProperty("mokkery.spike.compilerVersion").orNull?.let {
        @OptIn(org.jetbrains.kotlin.buildtools.api.ExperimentalBuildToolsApi::class)
        compilerVersion.set(it)
    }
    compilerOptions {
        freeCompilerArgs.add("-Xverify-ir=error")
    }
    jvm {
        compilerOptions {
            freeCompilerArgs.add("-Xemit-jvm-type-annotations")
        }
    }
}

tasks.withType<KotlinNativeLink>().configureEach {
    toolOptions.freeCompilerArgs.add("-Xverify-ir=warning")
}

tasks.withType<KotlinJsIrLink>().configureEach {
    compilerOptions.freeCompilerArgs.add("-Xverify-ir=warning")
}

dependencies {
    kotlinCompilerPluginClasspath(project(":mokkery-plugin"))
    kotlinNativeCompilerPluginClasspath(project(":mokkery-plugin"))
    commonMainImplementation(project(":mokkery-runtime"))
    commonMainImplementation(project(":mokkery-coroutines"))
    commonTestImplementation(kotlin("test"))
    commonTestImplementation(libs.kotlinx.coroutines.test)
}
