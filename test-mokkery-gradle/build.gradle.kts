@file:Suppress("UnstableApiUsage")

import org.jetbrains.kotlin.konan.target.HostManager
import org.jetbrains.kotlin.konan.target.KonanTarget

plugins {
    id("mokkery-jvm")
    id("com.github.gmazzo.buildconfig")
}

kotlin {
    optInMokkeryDelicateAndInternals()
}

val test by testing.suites.getting(JvmTestSuite::class) {
    useJUnitJupiter()
    targets.configureEach {
        testTask.configure {
            jvmArgs(
                "--add-opens=java.base/java.lang=ALL-UNNAMED",
                "--add-opens=java.base/java.lang.invoke=ALL-UNNAMED",
                "--add-opens=java.base/java.util=ALL-UNNAMED",
            )
        }
    }
}

dependencies {
    testImplementation(gradleApi())
    testImplementation(libs.kotlin.plugin)
    testImplementation(project(":mokkery-gradle"))
    testImplementation(libs.kotlin.test.junit5)
}

val functionalTest by testing.suites.creating(JvmTestSuite::class) {
    val compilations = kotlin.target.compilations
    compilations.getByName("functionalTest").associateWith(compilations.getByName("main"))
    useJUnitJupiter()
    targets.configureEach {
        testTask.configure {
            testLogging.showStandardStreams = true
            mustRunAfter("test")
            val repoName = testingRepoName
            dependsOnPublishMultiplatformTo(":mokkery-core", repoName)
            dependsOnPublishMultiplatformTo(":mokkery-runtime", repoName)
            dependsOnPublishMultiplatformTo(":mokkery-coroutines", repoName)
            dependsOnGradlePublishTo(":mokkery-gradle", repoName, "Mokkery")
            dependsOnPublishTo(":mokkery-core-tooling", repoName)
            dependsOnPublishTo(":mokkery-plugin", repoName)
        }
    }
    dependencies {
        implementation(project(":mokkery-core-tooling"))
        implementation(gradleTestKit())
    }
}

buildConfig {
    functionalTest.sources {
        packageName("dev.mokkery.gradle")
        buildConfigField("TESTING_REPO_URL", project.testingRepoUrl)
    }
}

tasks.check { dependsOn(functionalTest) }


private fun Test.dependsOnPublishTo(project: String, repoName: String) {
    val repoInTaskName = repoName.toRepoInTaskName()
    dependsOn(project(project).tasks.named("publishMavenPublicationTo$repoInTaskName"))
}

private fun Test.dependsOnGradlePublishTo(project: String, repoName: String, pluginPublicationName: String) {
    val repoInTaskName = repoName.toRepoInTaskName()
    dependsOn(project(project).tasks.named("publishMavenPublicationTo$repoInTaskName"))
    dependsOn(project(project).tasks.named("publish${pluginPublicationName}PluginMarkerMavenPublicationTo$repoInTaskName"))
}

private fun Test.dependsOnPublishMultiplatformTo(project: String, repoName: String) {
    val repoInTaskName = repoName.toRepoInTaskName()
    fun dependsOnPublishPublicationTo(name: String) = dependsOn(
        project(project)
            .tasks
            .named("publish${name}PublicationTo$repoInTaskName")
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

private fun String.toRepoInTaskName() = replaceFirstChar { it.uppercase() } + "Repository"
