@file:Suppress("UnstableApiUsage")

import org.jetbrains.kotlin.gradle.dsl.KotlinVersion
import org.jetbrains.kotlin.konan.target.HostManager
import org.jetbrains.kotlin.konan.target.KonanTarget

plugins {
    kotlin("jvm")
    id("com.github.gmazzo.buildconfig")
    id("mokkery-publish")
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

val functionalTest by testing.suites.creating(JvmTestSuite::class) {
    val compilations = kotlin.target.compilations
    compilations.getByName("functionalTest").associateWith(compilations.getByName("main"))
    useJUnitJupiter()
    targets.configureEach {
        testTask.configure {
            testLogging.showStandardStreams = true
            mustRunAfter("test")
            val repoName = "TestingRepository"
            dependsOnPublishMultiplatformTo(":mokkery-core", repoName)
            dependsOnPublishMultiplatformTo(":mokkery-runtime", repoName)
            dependsOnPublishMultiplatformTo(":mokkery-coroutines", repoName)
            dependsOnGradlePublishTo(":mokkery-gradle", repoName)
            dependsOnPublishTo(":mokkery-core-tooling", repoName)
            dependsOnPublishTo(":mokkery-plugin", repoName)
        }
    }
    dependencies {
        implementation(gradleTestKit())
    }
}

buildConfig {
    functionalTest.sources {
        packageName("dev.mokkery.gradle")
        val testingRepository = publishing.repositories
            .find { it.name == "testing" }
            .let { it as MavenArtifactRepository }
        buildConfigField("TESTING_REPO_URL", testingRepository.url)
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
    testSourceSet(functionalTest.sources)
}

private fun Test.dependsOnPublishTo(project: String, repoName: String) {
    dependsOn(project(project).tasks.named("publishMavenPublicationTo$repoName"))
}

private fun Test.dependsOnGradlePublishTo(project: String, repoName: String) {
    dependsOn(project(project).tasks.named("publishMavenPublicationTo$repoName"))
    dependsOn(project(project).tasks.named("publishMokkeryPluginMarkerMavenPublicationTo$repoName"))
}

private fun Test.dependsOnPublishMultiplatformTo(project: String, repoName: String) {
    fun dependsOnPublishPublicationTo(name: String) = dependsOn(
        project(project)
            .tasks
            .named("publish${name}PublicationTo$repoName")
    )

    dependsOnPublishPublicationTo("KotlinMultiplatform")
    dependsOnPublishPublicationTo("Jvm")
    dependsOnPublishPublicationTo("WasmJs")
    dependsOnPublishPublicationTo("Js")
    when (HostManager.host) {
        is KonanTarget.LINUX_X64 -> dependsOnPublishPublicationTo("LinuxX64")
        is KonanTarget.LINUX_ARM64 -> dependsOnPublishPublicationTo("LinuxArm64")
        is KonanTarget.MACOS_X64 -> {
            dependsOnPublishPublicationTo("MacosX64")
        }
        is KonanTarget.MACOS_ARM64 -> {
            dependsOnPublishPublicationTo("MacosArm64")
            dependsOnPublishPublicationTo("IosSimulatorArm64")
        }
        is KonanTarget.MINGW_X64 -> dependsOnPublishPublicationTo("MingwX64")
        else -> error("Unsupported target ${HostManager.host}")
    }
}
