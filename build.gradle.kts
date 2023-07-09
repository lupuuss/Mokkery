@file:Suppress("OPT_IN_USAGE")

import org.jetbrains.dokka.gradle.DokkaMultiModuleTask
import org.jetbrains.kotlin.gradle.dsl.kotlinExtension
import org.jetbrains.kotlin.gradle.plugin.kotlinToolingVersion
import org.jetbrains.kotlin.tooling.core.toKotlinVersion

plugins {
    alias(libs.plugins.dokka)
}

buildscript {
    repositories {
        mavenCentral()
        google()
        maven {
            url = uri("https://plugins.gradle.org/m2/")
        }
    }

    dependencies {
        classpath(":build-mokkery")
        classpath(libs.gradle.plugin.kotlinx.atomicfu)
    }
}

rootProject.version = "1.0"
rootProject.group = "dev.mokkery"

val supportedKotlinVersions = listOf(
    KotlinVersion(1, 8, 21),
    KotlinVersion(1, 8, 22),
    KotlinVersion(1, 9, 0),
)

if (kotlinToolingVersion.toKotlinVersion() !in supportedKotlinVersions) {
    error("Unsupported kotlin version! Current: $kotlinToolingVersion Supported: $supportedKotlinVersions")
}

rootProject.ext["kotlinVersion"] = libs.versions.kotlin.get()
rootProject.ext["supportedKotlinVersions"] = supportedKotlinVersions
rootProject.ext["pluginVersion"] = "${libs.versions.kotlin.get()}-$version"
rootProject.ext["pluginId"] = "dev.mokkery"

allprojects {
    group = rootProject.group
    version = rootProject.version
    if (name in listOf("mokkery-plugin")) {
        version = rootProject.ext["pluginVersion"]!!
    }
    afterEvaluate {
        val minimumVersion = supportedKotlinVersions.first()
        if (name in listOf("mokkery-core")) {
            kotlinExtension.sourceSets.all {
                languageSettings.languageVersion = minimumVersion.run { "$major.$minor" }
            }
        }
        extensions.findByType<JavaPluginExtension>()?.apply {
            toolchain.languageVersion.set(JavaLanguageVersion.of(8))
        }
    }
}

val dokkaHtmlMultiModule by tasks.getting(DokkaMultiModuleTask::class)
val syncDocs by tasks.registering(Sync::class) {
    from(dokkaHtmlMultiModule.outputDirectory)
    into(rootProject.layout.projectDirectory.dir("docs"))
}
