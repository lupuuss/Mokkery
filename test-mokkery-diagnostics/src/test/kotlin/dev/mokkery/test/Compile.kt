package dev.mokkery.test

import com.tschuchort.compiletesting.JvmCompilationResult
import com.tschuchort.compiletesting.KotlinCompilation
import com.tschuchort.compiletesting.SourceFile
import dev.mokkery.plugin.MokkeryCompilerPluginRegistrar
import org.intellij.lang.annotations.Language
import kotlin.test.assertContains
import kotlin.test.assertEquals
import kotlin.test.assertTrue

fun compileJvm(@Language("kotlin") file: String): JvmCompilationResult {
    val source = SourceFile.kotlin("main.kt", file)
    val compilation = KotlinCompilation().apply {
        sources = listOf(source)
        compilerPluginRegistrars = listOf(MokkeryCompilerPluginRegistrar())
        inheritClassPath = true
        messageOutputStream = System.out
        kotlincArguments += "-Xcontext-parameters"
    }
    return compilation.compile()
}

fun JvmCompilationResult.assertSingleError(message: String, level: String = "e:") {
    assertEquals(KotlinCompilation.ExitCode.COMPILATION_ERROR, exitCode)
    messages
        .split("\n")
        .single { it.isNotBlank() }
        .let {
            assertTrue { it.startsWith(level) }
            assertContains(it, message)
        }
}
