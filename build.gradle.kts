import com.github.gmazzo.gradle.plugins.BuildConfigExtension

plugins {
    alias(libs.plugins.kotlin.jvm) apply false
    alias(libs.plugins.kotlin.kapt) apply false
    alias(libs.plugins.kotlin.multiplatform) apply false
    alias(libs.plugins.buildconfig) apply false
}

buildscript {
    dependencies {
        classpath(libs.gradle.plugin.kotlinx.atomicfu)
    }
}

allprojects {
    group = "dev.mokkery"
    version = "1.0"
    ext["pluginId"] = "dev.mokkery.plugin"

    afterEvaluate {
        extensions.findByType<JavaPluginExtension>()?.apply {
            toolchain.languageVersion.set(JavaLanguageVersion.of(11))
        }
        extensions.findByType<BuildConfigExtension>()?.apply {
            val project = project(":mokkery-plugin")
            packageName(project.group.toString())

            buildConfigField("String", "MOKKERY_GROUP", "\"${project.group}\"")
            buildConfigField("String", "MOKKERY_PLUGIN_ARTIFACT_ID", "\"${project.name}\"")
            buildConfigField("String", "MOKKERY_VERSION", "\"${project.version}\"")
            buildConfigField("String", "MOKKERY_PLUGIN_ID", "\"${project.ext["pluginId"]}\"")
            buildConfigField("String", "MOKKERY_RUNTIME", "\"mokkery-runtime\"")
        }
    }
}
