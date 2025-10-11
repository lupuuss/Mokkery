@file:Suppress("UnstableApiUsage")

import org.jetbrains.kotlin.konan.target.HostManager
import org.jetbrains.kotlin.konan.target.KonanTarget


plugins {
    kotlin("jvm")
    id("mokkery-publish")
    alias(libs.plugins.gradle.portal.publish)
}

dependencies {
    compileOnly(kotlin("gradle-plugin"))
    api(project(":mokkery-core"))
}

kotlin {
    explicitApi()
    sourceSets.all {
        languageSettings.optIn("dev.mokkery.annotations.InternalMokkeryApi")
    }
}

fun Test.dependsOnPublishToMavenLocalOf(project: String) {
    dependsOn(project(project).tasks.named("publishToMavenLocal"))
}

fun Test.dependsOnPublishPublicationToMavenLocalOf(project: String, name: String) {
    dependsOn(project(project).tasks.named("publish${name}PublicationToMavenLocal"))
}

val functionalTest by testing.suites.creating(JvmTestSuite::class) {
    val compilations = kotlin.target.compilations
    compilations.getByName("functionalTest").associateWith(compilations.getByName("main"))
    useJUnitJupiter()
    targets.configureEach {
        testTask.configure {
            testLogging.showStandardStreams = true
            mustRunAfter("test")
            listOf(
                ":mokkery-core",
                ":mokkery-runtime",
                ":mokkery-coroutines"
            ).forEach {
                if (it == ":mokkery-runtime") {
                    dependsOnPublishPublicationToMavenLocalOf(it, "AndroidRelease")
                }
                dependsOnPublishPublicationToMavenLocalOf(it, "KotlinMultiplatform")
                dependsOnPublishPublicationToMavenLocalOf(it, "Jvm")
                dependsOnPublishPublicationToMavenLocalOf(it, "WasmJs")
                dependsOnPublishPublicationToMavenLocalOf(it, "Js")
                when (HostManager.host) {
                    is KonanTarget.LINUX_X64 -> dependsOnPublishPublicationToMavenLocalOf(it, "LinuxX64")
                    is KonanTarget.LINUX_ARM64 -> dependsOnPublishPublicationToMavenLocalOf(it, "LinuxArm64")
                    is KonanTarget.MACOS_X64 -> {
                        dependsOnPublishPublicationToMavenLocalOf(it, "MacosX64")
                    }
                    is KonanTarget.MACOS_ARM64 -> {
                        dependsOnPublishPublicationToMavenLocalOf(it, "MacosArm64")
                        dependsOnPublishPublicationToMavenLocalOf(it, "IosSimulatorArm64")
                    }
                    is KonanTarget.MINGW_X64 -> dependsOnPublishPublicationToMavenLocalOf(it, "MingwX64")
                    else -> error("Unsupported target ${HostManager.host}")
                }
            }
            dependsOnPublishToMavenLocalOf(":mokkery-plugin")
            dependsOnPublishToMavenLocalOf(":mokkery-gradle")
        }
    }
    dependencies {
        implementation(gradleTestKit())
    }
}

tasks.check { dependsOn(functionalTest) }

gradlePlugin {
    website.set(MokkeryAttributes.WebsiteUrl)
    vcsUrl.set(MokkeryAttributes.GitVscUrl)
    plugins {
        create(rootProject.name) {
            id = rootProject.extra["pluginId"] as String
            displayName = MokkeryAttributes.DisplayName
            description = MokkeryAttributes.Description
            version = project.version
            implementationClass = "${project.group}.gradle.MokkeryGradlePlugin"
            tags.set(
                listOf(
                    "kotlin",
                    "mock",
                    "test",
                    "kotlin-multiplatform",
                    "kotlin-multiplatform-mobile",
                    "kotlin-compiler-plugin"
                )
            )
        }
    }
    testSourceSet(functionalTest.sources)
}
