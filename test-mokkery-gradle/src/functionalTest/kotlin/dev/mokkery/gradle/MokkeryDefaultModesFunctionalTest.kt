package dev.mokkery.gradle

import dev.mokkery.internal.MokkeryConfig
import org.intellij.lang.annotations.Language
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File

class MokkeryDefaultModesFunctionalTest {

    @field:TempDir
    private lateinit var projectDir: File

    @Test
    fun `passes the compiler defaults to the runtime when modes are not configured`() {
        val build = project(modes = "")
        build.file(testSource, modesTest(strict, soft))
        build.build(testTask)
    }

    @Test
    fun `passes the configured default modes to the runtime`() {
        val build = project(modes = modes(autoUnit, exhaustive))
        build.file(testSource, modesTest(autoUnit, exhaustive))
        build.build(testTask)

        build.file(buildScriptPath, buildScript(modes(autofill, exactlyTwice)))
        build.file(testSource, modesTest(autofill, exactlyTwice))
        build.build(testTask)
    }

    private fun project(modes: String) = gradleBuild(projectDir) {
        settings(kotlinPlugin = "jvm", projectName = "default-modes")
        file(buildScriptPath, buildScript(modes))
        file(mainSource, modesInterface)
    }
}

private const val packageName = "dev.mokkery.modes"
private const val buildScriptPath = "build.gradle.kts"
private const val mainSource = "src/main/kotlin/dev/mokkery/modes/Modes.kt"
private const val testSource = "src/test/kotlin/dev/mokkery/modes/DefaultModesTest.kt"
private const val testTask = ":test"

private val strict = Mode(
    expression = "MockMode.strict",
    behaviour = """
        val mock = mock<Modes>()
        assertFails { mock.value() }
        assertFails { mock.action() }
    """
)

private val autoUnit = Mode(
    expression = "MockMode.autoUnit",
    behaviour = """
        val mock = mock<Modes>()
        mock.action()
        assertFails { mock.value() }
    """
)

private val autofill = Mode(
    expression = "MockMode.autofill",
    behaviour = """
        assertEquals(0, mock<Modes>().value())
        assertFails { mock<Modes>(MockMode.strict).value() }
    """
)

private val soft = Mode(
    expression = "VerifyMode.soft",
    behaviour = """
        val mock = mock<Modes>(MockMode.autoUnit)
        mock.action()
        mock.other()
        verify { mock.action() }
    """
)

private val exhaustive = Mode(
    expression = "VerifyMode.exhaustive",
    behaviour = """
        val mock = mock<Modes>(MockMode.autoUnit)
        mock.action()
        mock.other()
        assertFails { verify { mock.action() } }
        verify {
            mock.action()
            mock.other()
        }
    """
)

private val exactlyTwice = Mode(
    expression = "VerifyMode.exactly(2)",
    behaviour = """
        val mock = mock<Modes>(MockMode.autoUnit)
        mock.action()
        assertFails { verify { mock.action() } }
        mock.action()
        verify { mock.action() }
        val overridden = mock<Modes>(MockMode.autoUnit)
        overridden.action()
        verify(VerifyMode.soft) { overridden.action() }
    """
)

private class Mode( @Language("kt") val expression: String, @Language("kt") val behaviour: String)

private fun modes(mockMode: Mode, verifyMode: Mode) = """
    defaultMockMode = ${mockMode.expression}
    defaultVerifyMode = ${verifyMode.expression}
"""

@Language("kts")
private fun buildScript(modes: String) = """
    import dev.mokkery.MockMode
    import dev.mokkery.verify.VerifyMode

    plugins {
        kotlin("jvm")
        id("dev.mokkery")
    }

    mokkery {
        $modes
    }

    dependencies {
        testImplementation(kotlin("test"))
    }

    tasks.test {
        useJUnitPlatform()
    }
"""

private val modesInterface = """
    package $packageName

    interface Modes {
        fun value(): Int
        fun action()
        fun other()
    }
"""

private fun modesTest(mockMode: Mode, verifyMode: Mode) = """
    @file:OptIn(InternalMokkeryApi::class)

    package $packageName

    import dev.mokkery.MockMode
    import dev.mokkery.MokkeryScope
    import dev.mokkery.annotations.InternalMokkeryApi
    import dev.mokkery.internal.defaultMockMode
    import dev.mokkery.internal.defaultVerifyMode
    import dev.mokkery.internal.mokkeryInternals
    import dev.mokkery.mock
    import dev.mokkery.module
    import dev.mokkery.verify
    import dev.mokkery.verify.VerifyMode
    import kotlin.test.Test
    import kotlin.test.assertEquals
    import kotlin.test.assertFails

    class DefaultModesTest {

        private val settings = MokkeryScope.module.mokkeryInternals

        @Test
        fun passesDefaultMockMode() {
            assertEquals(${mockMode.expression}, settings.defaultMockMode)
        }

        @Test
        fun passesDefaultVerifyMode() {
            assertEquals(${verifyMode.expression}, settings.defaultVerifyMode)
        }

        @Test
        fun appliesDefaultMockMode() {
            ${mockMode.behaviour.trimIndent()}
        }

        @Test
        fun appliesDefaultVerifyMode() {
            ${verifyMode.behaviour.trimIndent()}
        }
    }
"""
