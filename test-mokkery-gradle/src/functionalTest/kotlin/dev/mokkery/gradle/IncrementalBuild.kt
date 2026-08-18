package dev.mokkery.gradle

import org.gradle.testkit.runner.BuildResult
import org.gradle.testkit.runner.GradleRunner
import java.io.File

class IncrementalBuild(
    private val projectDir: File,
    private val properties: Map<String, String> = emptyMap(),
) {

    private val reportsDir = projectDir.resolve("build-reports")

    fun file(path: String, content: String) {
        projectDir
            .resolve(path)
            .apply { parentFile.mkdirs() }
            .writeText(content.trimIndent())
    }

    fun delete(path: String) {
        check(projectDir.resolve(path).delete()) { "Failed to delete $path" }
    }

    fun build(vararg tasks: String) = run(tasks, GradleRunner::build)

    fun buildAndFail(vararg tasks: String) = run(tasks, GradleRunner::buildAndFail)

    private fun run(tasks: Array<out String>, execute: (GradleRunner) -> BuildResult): IncrementalBuildResult {
        reportsDir.deleteRecursively()
        val runner = GradleRunner
            .create()
            .withProjectDir(projectDir)
            .withArguments(tasks.toList() + buildArguments())
            .forwardOutput()
        return IncrementalBuildResult(execute(runner), compiledSourcesByTask())
    }

    private fun buildArguments() = properties.map { (name, value) -> "-P$name=$value" } + listOf(
        "-Porg.gradle.jvmargs=-Xmx1g",
        "-Pkotlin.daemon.jvmargs=-Xmx1g",
        "-Pkotlin.incremental=true",
        "-Pkotlin.build.report.output=file",
        "-Pkotlin.build.report.file.output_dir=${reportsDir.invariantSeparatorsPath}",
        "-Pkotlin.build.report.verbose=true",
    )

    private fun compiledSourcesByTask(): Map<String, Set<String>> = reportsDir
        .listFiles()
        .orEmpty()
        .ifEmpty { error("No Kotlin build report written to $reportsDir") }
        .flatMap { parseCompiledSources(it.readText()).entries }
        .associate { (task, sources) -> task to sources }

    private fun parseCompiledSources(report: String): Map<String, Set<String>> {
        val headers = TASK_HEADER.findAll(report).toList()
        return headers.withIndex().associate { (index, header) ->
            val end = headers.getOrNull(index + 1)?.range?.first ?: report.length
            header.groupValues[1] to compiledSourcesIn(report.substring(header.range.last + 1, end))
        }
    }

    private fun compiledSourcesIn(taskSection: String) = taskSection
        .splitToSequence(COMPILE_ITERATION)
        .drop(1)
        .flatMap { it.lineSequence().drop(1).takeWhile { line -> line.startsWith(SOURCE_INDENT) } }
        .map { it.trim().substringBefore(" <- ").toProjectPath() }
        .toSet()

    private fun String.toProjectPath() = replace(File.separatorChar, '/')
        .removePrefix("${projectDir.invariantSeparatorsPath}/")
}

class IncrementalBuildResult(
    private val result: BuildResult,
    private val sourcesByTask: Map<String, Set<String>>,
) {

    val output: String get() = result.output

    fun compiledSources(task: String): Set<String> {
        checkNotNull(result.task(task)) { "Task $task did not run. Executed: ${result.tasks.map { it.path }}" }
        return sourcesByTask[task].orEmpty()
    }
}

private val TASK_HEADER = Regex("^Task '(:[^']+)' finished", RegexOption.MULTILINE)
private const val COMPILE_ITERATION = "Compile iteration:"
private const val SOURCE_INDENT = "    "
