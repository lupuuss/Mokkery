@file:Suppress("UnstableApiUsage")

import org.jetbrains.kotlin.gradle.dsl.KotlinVersion

plugins {
    id("mokkery-jvm")
    id("mokkery-publish")
    alias(libs.plugins.gradle.portal.publish)
}

dependencies {
    compileOnly(kotlin("gradle-plugin"))
    compileOnly(libs.kotlin.stdlib)
    compileOnly(project(":mokkery-gradle"))
    api(project(":mokkery-mockable-tooling")) {
        isTransitive = false
    }
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
        create("mokkeryMockable") {
            id = "${MokkeryAttributes.PluginId}.mockable"
            displayName = MokkeryAttributes.DisplayName
            description = MokkeryAttributes.Description
            version = project.version
            implementationClass = "dev.mokkery.mockable.gradle.MokkeryMockableGradlePlugin"
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
