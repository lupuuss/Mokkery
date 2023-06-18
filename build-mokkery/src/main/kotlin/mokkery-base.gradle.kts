plugins {
    id("com.github.gmazzo.buildconfig")
    `maven-publish`
}

buildConfig {
    val project = project(":mokkery-plugin")
    packageName(project.group.toString())

    buildConfigField("String", "MOKKERY_GROUP", "\"${project.group}\"")
    buildConfigField("String", "MOKKERY_PLUGIN_ARTIFACT_ID", "\"${project.name}\"")
    buildConfigField("String", "MOKKERY_VERSION", "\"${project.version}\"")
    buildConfigField("String", "MOKKERY_PLUGIN_ID", "\"${project.ext["pluginId"]}\"")
    buildConfigField("String", "MOKKERY_RUNTIME", "\"mokkery-runtime\"")
}
