plugins {
    id("mokkery-publish")
    id("mokkery-multiplatform")
    id("com.github.gmazzo.buildconfig")
}

buildConfig {
    val pluginProject = project(":mokkery-plugin")
    packageName(rootProject.group.toString())
    buildConfigField("String", "GROUP", str(rootProject.group))
    buildConfigField("String", "VERSION", str(rootProject.version))
    buildConfigField("String", "RUNTIME", str("mokkery-runtime"))
    buildConfigField("String", "PLUGIN_ID", str(rootProject.ext["pluginId"]))
    buildConfigField("String", "PLUGIN_ARTIFACT_ID", str(pluginProject.name))
    buildConfigField("String", "MINIMUM_KOTLIN_VERSION", str("2.1.0"))
    buildConfigField("String", "COMPILED_KOTLIN_VERSION", str(libs.versions.kotlin.get()))
}

private fun str(value: Any?) = "\"$value\""
