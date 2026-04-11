package dev.mokkery.tests

import org.jetbrains.kotlin.platform.wasm.WasmTarget
import org.jetbrains.kotlin.test.services.KotlinStandardLibrariesPathProvider
import java.io.File

object ClasspathBasedStandardLibrariesPathProvider : KotlinStandardLibrariesPathProvider {
    private val SEP = "\\${File.separator}"

    private val GRADLE_DEPENDENCY =
        (".*?" +
                SEP +
                "(?<name>[^$SEP]*)" +
                SEP +
                "(?<version>[^$SEP]*)" +
                SEP +
                "[^$SEP]*" +
                SEP +
                "\\1-\\2\\.jar")
            .toRegex()

    private val jars =
        System.getProperty("java.class.path")
            .split("\\${File.pathSeparator}".toRegex())
            .dropLastWhile(String::isEmpty)
            .map(::File)
            .associateBy { file ->
                GRADLE_DEPENDENCY.matchEntire(file.path)?.let { it.groups["name"]!!.value } ?: file.name
            }

    private fun getFile(name: String) = jars[name] ?: error("Jar $name not found in classpath:\n${jars.entries.joinToString("\n")}")

    override fun runtimeJarForTests(): File = getFile("kotlin-stdlib")

    override fun runtimeJarForTestsWithJdk8(): File = getFile("kotlin-stdlib-jdk8")

    override fun minimalRuntimeJarForTests(): File = getFile("kotlin-stdlib")

    override fun commonStdlibForTests(): File = getFile("kotlin-stdlib-common")

    override fun reflectJarForTests(): File = getFile("kotlin-reflect")

    override fun kotlinTestJarForTests(): File = getFile("kotlin-test")

    override fun scriptRuntimeJarForTests(): File = getFile("kotlin-script-runtime")

    override fun jvmAnnotationsForTests(): File = getFile("kotlin-annotations-jvm")

    override fun getAnnotationsJar(): File = getFile("kotlin-annotations-jvm")

    override fun fullJsStdlib(): File = getFile("kotlin-stdlib-js")

    override fun defaultJsStdlib(): File = getFile("kotlin-stdlib-js")

    override fun kotlinTestJsKLib(): File = getFile("kotlin-test-js")

    override fun fullWasmStdlib(target: WasmTarget): File = getFile("kotlin.wasm.stdlib.${target.alias}.path")

    override fun kotlinTestWasmKLib(target: WasmTarget): File = getFile("kotlin.wasm.test.${target.alias}.path")

    override fun webStdlibForTests(): File = getFile("kotlin-stdlib-web")

    override fun scriptingPluginFilesForTests(): Collection<File> = TODO()
}
