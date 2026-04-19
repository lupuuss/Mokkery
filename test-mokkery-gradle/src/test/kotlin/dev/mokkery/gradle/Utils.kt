package dev.mokkery.gradle

import org.gradle.api.Project
import org.gradle.api.artifacts.ExternalDependency
import org.gradle.api.internal.project.ProjectInternal
import org.intellij.lang.annotations.Language
import java.nio.file.Path
import kotlin.io.path.createDirectories
import kotlin.io.path.createFile
import kotlin.io.path.writeText

fun Project.evaluate() {
    (this as ProjectInternal).evaluate()
}

fun Project.requireDependency(configuration: String, dependency: String) {
    val (group, name, version) = dependency.split(':')
    val configuration = project.configurations.getByName(configuration)
    val dependencies = configuration.dependencies
    val result = dependencies
        .filterIsInstance<ExternalDependency>()
        .find { it.group == group && it.name == name && it.version == version }
    assert(result != null) {
        "Expected dependency $dependency but haven't found!" +
                " configuration: ${configuration.name}, dependencies: ${dependencies.toList()}"
    }
}

fun Project.requireNoDependency(configuration: String, dependency: String) {
    val (group, name, version) = dependency.split(':')
    val configuration = project.configurations.getByName(configuration)
    val dependencies = configuration.dependencies
    val result = dependencies
        .filterIsInstance<ExternalDependency>()
        .find { it.group == group && it.name == name && it.version == version }
    assert(result == null) {
        "Dependency $dependency was not expected, but found in ${configuration.name}!"
    }
}

fun Project.fakeRepository(
    path: Path,
    vararg dependencies: String
) {
    dependencies
        .map { it.split(":") }
        .forEach { (group, artifactId, version) ->
            val artifactDir = path.fakeArtifactDir(group, artifactId, version)
            artifactDir.fakeJar(artifactId, version)
            artifactDir.fakePom(
                artifactId = artifactId,
                version = version,
                text = """
                <project>
                    <groupId>dev.mokkery</groupId>
                    <artifactId>mokkery-gradle</artifactId>
                    <version>$version</version>
                </project>
                """.trimIndent(),
            )
            buildscript.dependencies.add("classpath", "$group:$artifactId:$version")
        }
    buildscript.repositories.maven { it.url = path.toUri() }
}

fun Path.fakeArtifactDir(group: String, artifactId: String, version: String): Path = group
    .split(".")
    .plus(artifactId)
    .plus(version)
    .joinToString(separator = "/")
    .let(this::resolve)
    .createDirectories()

fun Path.fakeJar(artifactId: String, version: String): Path = resolve("$artifactId-$version.jar")
    .createFile()

fun Path.fakePom(artifactId: String, version: String, @Language("pom") text: String) {
    resolve("${artifactId}-$version.pom").writeText(text)
}
