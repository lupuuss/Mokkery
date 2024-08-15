plugins {
    id("mokkery-publish")
    id("mokkery-multiplatform")
    id("com.github.gmazzo.buildconfig")
}

buildConfig {
    val pluginProject = project(":mokkery-plugin")
    packageName(rootProject.group.toString())
    buildConfigField("String", "GROUP", "\"${rootProject.group}\"")
    buildConfigField("String", "VERSION", "\"${rootProject.version}\"")
    buildConfigField("String", "RUNTIME", "\"mokkery-runtime\"")
    buildConfigField("String", "PLUGIN_ID", "\"${rootProject.ext["pluginId"]}\"")
    buildConfigField("String", "PLUGIN_ARTIFACT_ID", "\"${pluginProject.name}\"")
}
