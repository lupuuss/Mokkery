@file:Suppress("UnstableApiUsage")

import org.jetbrains.kotlin.gradle.dsl.KotlinVersion

plugins {
    id("mokkery-jvm")
    id("mokkery-publish")
    id("com.github.gmazzo.buildconfig")
    alias(libs.plugins.gradle.portal.publish)
}

dependencies {
    compileOnly(kotlin("gradle-plugin"))
    compileOnly(libs.kotlin.stdlib)
    api(project(":mokkery-core"))
    api(project(":mokkery-core-tooling"))
}

kotlin {
    explicitApi()
    setKotlinCompatibility(KotlinVersion.KOTLIN_2_2)
    sourceSets.all {
        languageSettings.optIn("dev.mokkery.annotations.InternalMokkeryApi")
    }
}

gradlePlugin {
    website.set(MokkeryAttributes.WebsiteUrl)
    vcsUrl.set(MokkeryAttributes.GitVscUrl)
    plugins {
        create("mokkery") {
            id = MokkeryAttributes.PluginId
            displayName = MokkeryAttributes.DisplayName
            description = MokkeryAttributes.Description
            version = project.version
            implementationClass = "dev.mokkery.gradle.MokkeryGradlePlugin"
            tags.set(
                listOf(
                    "kotlin",
                    "mock",
                    "test",
                    "kotlin-multiplatform",
                    "kotlin-compiler-plugin"
                )
            )
        }
    }
}
