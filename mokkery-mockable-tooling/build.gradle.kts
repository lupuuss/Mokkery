@file:Suppress("UnstableApiUsage")

import org.jetbrains.kotlin.gradle.dsl.KotlinVersion

plugins {
    id("mokkery-jvm")
    id("com.github.gmazzo.buildconfig")
    id("mokkery-publish")
    alias(libs.plugins.poko)
}

dependencies {
    api(project(":mokkery-core-tooling"))
}

kotlin {
    explicitApi()
    setKotlinCompatibility(KotlinVersion.KOTLIN_2_2)
    sourceSets.all {
        languageSettings.optIn("dev.mokkery.annotations.InternalMokkeryApi")
    }
}

buildConfig {
    val pluginId = "${MokkeryAttributes.PluginId}.mockable"
    packageName("dev.mokkery.internal")
    buildConfigField("String", "GROUP", str(project.group))
    buildConfigField("String", "VERSION", str(project.version))
    buildConfigField("String", "ANNOTATIONS", str("mokkery-mockable-annotations"))
    buildConfigField("String", "PLUGIN_ID", str(pluginId))
    buildConfigField("String", "PLUGIN_ARTIFACT_ID", str("mokkery-mockable-plugin"))
}

private fun str(value: Any?) = "\"$value\""
