import org.jetbrains.kotlin.gradle.plugin.SubpluginOption
import org.jetbrains.kotlin.gradle.tasks.AbstractKotlinCompile
import org.jetbrains.kotlin.gradle.tasks.KotlinNativeCompile

plugins {
    id("mokkery-publish")
    id("mokkery-multiplatform")
    alias(libs.plugins.poko)
    alias(libs.plugins.kotlinx.atomicfu)
    alias(libs.plugins.agp.library)
}

kotlin {
    androidTarget {
        publishLibraryVariants("release")
    }
}

android {
    compileSdk = 36
    namespace = rootProject.group.toString()
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
    commonMainCompileOnly(libs.kotlin.stdlib)

    jsMainCompileOnly(libs.kotlin.dom.api.compat)

    jvmMainImplementation(libs.objenesis)
    jvmMainImplementation(libs.bytebuddy)

    val androidMainImplementation by configurations.getting
    androidMainImplementation(libs.kotlin.stdlib)
    androidMainImplementation(libs.objenesis)
    androidMainImplementation(libs.bytebuddy)
    androidMainImplementation(libs.dexmaker)
    // workaround for https://github.com/Kotlin/kotlinx-atomicfu/issues/145
    androidMainImplementation("org.jetbrains.kotlinx:atomicfu:${libs.versions.atomicfu.get()}")

    commonTestImplementation(kotlin("test"))
}
