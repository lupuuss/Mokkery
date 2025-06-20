import org.jetbrains.kotlin.gradle.plugin.SubpluginOption
import org.jetbrains.kotlin.gradle.tasks.AbstractKotlinCompile
import org.jetbrains.kotlin.gradle.tasks.KotlinNativeCompile

plugins {
    id("mokkery-publish")
    id("mokkery-multiplatform")
    alias(libs.plugins.poko)
    alias(libs.plugins.kotlinx.atomicfu)
}

kotlin.compilerOptions.freeCompilerArgs.add("-Xcontext-parameters")

val options = org.jetbrains.kotlin.gradle.tasks.CompilerPluginOptions()

options.addPluginArgument("dev.mokkery", SubpluginOption(key = "mockMode", value = "strict"))
options.addPluginArgument("dev.mokkery", SubpluginOption(key = "verifyMode", value = "ExhaustiveOrder"))
options.addPluginArgument("dev.mokkery", SubpluginOption(key = "ignoreFinalMembers", value = "false"))
options.addPluginArgument("dev.mokkery", SubpluginOption(key = "ignoreInlineMembers", value = "false"))

tasks.withType<AbstractKotlinCompile<*>> {
    pluginOptions.add(options)
}

tasks.withType<KotlinNativeCompile> {
    compilerPluginOptions.addPluginArgument(options)
}
dependencies {
    kotlinCompilerPluginClasspath(project(":mokkery-plugin"))
    kotlinNativeCompilerPluginClasspath(project(":mokkery-plugin"))
    kotlinNativeCompilerPluginClasspath(project(":mokkery-core"))
    commonMainApi(project(":mokkery-core"))
    commonTestImplementation(kotlin("test"))
    jvmMainImplementation(libs.objenesis)
    jvmMainImplementation(libs.bytebuddy)
}
