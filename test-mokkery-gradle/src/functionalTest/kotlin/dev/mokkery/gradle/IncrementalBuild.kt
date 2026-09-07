package dev.mokkery.gradle

import org.gradle.testkit.runner.BuildResult
import java.io.File

fun incrementalBuild(projectDir: File, configure: GradleBuild.() -> Unit) = IncrementalBuild(projectDir, configure)

class IncrementalBuild(projectDir: File, configure: GradleBuild.() -> Unit) {

    private val reportsDir = projectDir.resolve("build-reports")

    private val build = gradleBuild(projectDir) {
        property("kotlin.incremental", "true")
        property("kotlin.build.report.output", "file")
        property("kotlin.build.report.file.output_dir", reportsDir.invariantSeparatorsPath)
        property("kotlin.build.report.verbose", "true")
        configure()
    }

    fun file(path: String, content: String) = build.file(path, content)

    fun delete(path: String) = build.delete(path)

    fun build(vararg tasks: String) = run { build.build(*tasks) }

    fun buildAndFail(vararg tasks: String) = run { build.buildAndFail(*tasks) }

    private fun run(execute: () -> BuildResult): IncrementalBuildResult {
        reportsDir.deleteRecursively()
        return IncrementalBuildResult(execute(), compiledSourcesByTask())
    }

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
        .map { build.projectPathOf(File(it.trim().substringBefore(" <- "))) }
        .toSet()
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
