plugins {
    kotlin("jvm")
    id("com.github.gmazzo.buildconfig")
    id("mokkery-publish")
    alias(libs.plugins.poko)
}

kotlin {
    explicitApi()
    optInMokkeryDelicateAndInternals()
    compilerOptions {
        freeCompilerArgs.add("-Xcontext-parameters")
    }
}

dependencies {
    api(project(":mokkery-core"))
    testImplementation(kotlin("test"))
}

buildConfig {
    val pluginProject = project(":mokkery-plugin")
    packageName(rootProject.group.toString())
    buildConfigField("String", "GROUP", str(rootProject.group))
    buildConfigField("String", "VERSION", str(rootProject.version))
    buildConfigField("String", "RUNTIME", str("mokkery-runtime"))
    buildConfigField("String", "PLUGIN_ID", str(rootProject.ext["pluginId"]))
    buildConfigField("String", "PLUGIN_ARTIFACT_ID", str(pluginProject.name))
    buildConfigField("String", "MINIMUM_KOTLIN_VERSION", str(libs.versions.kotlinMininumSupported.get()))
    buildConfigField("String", "COMPILED_KOTLIN_VERSION", str(libs.versions.kotlin.get()))
}

private fun str(value: Any?) = "\"$value\""
