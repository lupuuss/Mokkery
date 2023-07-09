@file:Suppress("UNCHECKED_CAST")

plugins {
    id("mokkery-multiplatform")
    id("com.github.gmazzo.buildconfig")
}

buildConfig {
    val pluginProject = project(":mokkery-plugin")
    val supportedVersions = rootProject.ext["supportedKotlinVersions"] as List<String>
    packageName(rootProject.group.toString())
    buildConfigField(
        type = "String",
        name = "SUPPORTED_KOTLIN_VERSIONS",
        value = "\"" + supportedVersions.joinToString { it } + "\""
    )
    buildConfigField("String", "KOTLIN_VERSION", "\"${rootProject.ext["kotlinVersion"]}\"")
    buildConfigField("String", "GROUP", "\"${rootProject.group}\"")
    buildConfigField("String", "VERSION", "\"${rootProject.version}\"")
    buildConfigField("String", "RUNTIME", "\"mokkery-runtime\"")
    buildConfigField("String", "PLUGIN_ID", "\"${rootProject.ext["pluginId"]}\"")
    buildConfigField("String", "PLUGIN_ARTIFACT_ID", "\"${pluginProject.name}\"")
}
