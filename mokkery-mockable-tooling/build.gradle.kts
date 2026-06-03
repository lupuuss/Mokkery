@file:Suppress("UnstableApiUsage")

import org.jetbrains.kotlin.gradle.dsl.KotlinVersion

plugins {
    kotlin("jvm")
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
    val pluginProject = project(":mokkery-mockable-plugin")
    val pluginId = "${rootProject.ext["pluginId"]}.mockable"
    packageName("dev.mokkery.internal")
    buildConfigField("String", "GROUP", str(rootProject.group))
    buildConfigField("String", "VERSION", str(rootProject.version))
    buildConfigField("String", "ANNOTATIONS", str("mokkery-mockable-annotations"))
    buildConfigField("String", "PLUGIN_ID", str(pluginId))
    buildConfigField("String", "PLUGIN_ARTIFACT_ID", str(pluginProject.name))
}

private fun str(value: Any?) = "\"$value\""
