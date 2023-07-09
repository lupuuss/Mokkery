@file:Suppress("UNCHECKED_CAST")

plugins {
    id("com.github.gmazzo.buildconfig")
    `maven-publish`
}

buildConfig {
    val pluginProject = project(":mokkery-plugin")
    val supportedVersions = rootProject.ext["supportedKotlinVersions"] as List<String>
    packageName(rootProject.group.toString())
    buildConfigField(
        type = "String",
        name = "MOKKERY_SUPPORTED_KOTLIN_VERSIONS",
        value = "\"" + supportedVersions.joinToString { it } + "\""
    )
    buildConfigField("String", "MOKKERY_KOTLIN_VERSION", "\"${rootProject.ext["kotlinVersion"]}\"")
    buildConfigField("String", "MOKKERY_GROUP", "\"${rootProject.group}\"")
    buildConfigField("String", "MOKKERY_VERSION", "\"${rootProject.version}\"")
    buildConfigField("String", "MOKKERY_RUNTIME", "\"mokkery-runtime\"")
    buildConfigField("String", "MOKKERY_PLUGIN_ID", "\"${rootProject.ext["pluginId"]}\"")
    buildConfigField("String", "MOKKERY_PLUGIN_ARTIFACT_ID", "\"${pluginProject.name}\"")
}
