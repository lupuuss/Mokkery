package dev.mokkery.gradle

import dev.mokkery.internal.MokkeryConfig
import org.gradle.testkit.runner.BuildResult
import org.gradle.testkit.runner.GradleRunner
import org.intellij.lang.annotations.Language
import java.io.File
import kotlin.io.path.ExperimentalPathApi
import kotlin.io.path.copyToRecursively

fun gradleBuild(projectDir: File, configure: GradleBuild.() -> Unit) = GradleBuild(projectDir).apply(configure)

class GradleBuild(private val projectDir: File) {

    private val properties = linkedMapOf(
        "org.gradle.jvmargs" to "-Xmx1g",
        "kotlin.daemon.jvmargs" to "-Xmx1g",
    )
    private val arguments = mutableListOf<String>()

    fun property(name: String, value: String) {
        properties[name] = value
    }

    fun argument(value: String) {
        arguments += value
    }

    fun file(path: String, content: String) {
        projectDir
            .resolve(path)
            .apply { parentFile.mkdirs() }
            .writeText(content.trimIndent())
    }

    @OptIn(ExperimentalPathApi::class)
    fun copyRecursively(from: File, to: String) {
        from.toPath().copyToRecursively(
            target = projectDir.resolve(to).toPath(),
            followLinks = false,
            overwrite = true,
        )
    }

    fun delete(path: String) {
        check(projectDir.resolve(path).delete()) { "Failed to delete $path" }
    }

    fun build(vararg tasks: String) = run(tasks, GradleRunner::build)

    fun buildAndFail(vararg tasks: String) = run(tasks, GradleRunner::buildAndFail)

    fun projectPathOf(file: File) = file
        .invariantSeparatorsPath
        .removePrefix("${projectDir.invariantSeparatorsPath}/")

    private fun run(tasks: Array<out String>, execute: (GradleRunner) -> BuildResult) = execute(
        GradleRunner
            .create()
            .withProjectDir(projectDir)
            .withArguments(tasks.toList() + properties.map { (name, value) -> "-P$name=$value" } + arguments)
            .forwardOutput()
    )
}

fun GradleBuild.settings(
    kotlinPlugin: String,
    projectName: String,
    kotlinVersion: String = MokkeryConfig.MINIMUM_KOTLIN_VERSION,
    extraKotlinPlugins: List<String> = emptyList(),
) {
    property("kotlinVersion", kotlinVersion)
    property("mokkeryVersion", MokkeryConfig.VERSION)
    val plugins = listOf(
        "kotlin(\"$kotlinPlugin\") version kotlinVersion",
        "id(\"dev.mokkery\") version mokkeryVersion",
    ) + extraKotlinPlugins.map { "id(\"$it\") version kotlinVersion" }
    file("settings.gradle.kts", settingsScript(plugins, projectName))
}

@Language("kts")
private fun settingsScript(plugins: List<String>, projectName: String) = """
    pluginManagement {
        val kotlinVersion: String by settings
        val mokkeryVersion: String by settings
        plugins {
            ${plugins.joinToString(separator = "\n            ")}
        }
        repositories {
            mavenCentral {
                content { excludeGroup("dev.mokkery") }
            }
            maven {
                name = "testing"
                url = uri("${BuildConfig.TESTING_REPO_URL}")
            }
        }
    }

    dependencyResolutionManagement {
        repositories {
            mavenCentral {
                content { excludeGroup("dev.mokkery") }
            }
            maven {
                name = "testing"
                url = uri("${BuildConfig.TESTING_REPO_URL}")
            }
        }
    }

    rootProject.name = "$projectName"
"""
