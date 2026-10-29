package dev.mokkery.gradle

import org.intellij.lang.annotations.Language
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File

class MokkeryLargeTypesFunctionalTest {

    @field:TempDir
    private lateinit var projectDir: File

    @Test
    fun `mocks and spies types with many methods`() {
        val build = gradleBuild(projectDir) {
            settings(kotlinPlugin = "multiplatform", projectName = "large-types")
            file("build.gradle.kts", buildScript)
            file("src/commonMain/kotlin/dev/mokkery/large/Huge.kt", hugeSource())
            file("src/commonTest/kotlin/dev/mokkery/large/HugeTest.kt", hugeTest())
        }
        build.build("kotlinUpgradeYarnLock", "allTests")
    }
}

private const val methodsCount = 750
private const val twinMethodsCount = 50
private val sampledMethods = ((0 until methodsCount step 83) + (methodsCount - 1)).distinct()
private val sampledTwinMethods = listOf(0, twinMethodsCount / 2, twinMethodsCount - 1)

@Language("kts")
private val buildScript = """
    import org.jetbrains.kotlin.konan.target.HostManager
    import org.jetbrains.kotlin.konan.target.KonanTarget

    plugins {
        kotlin("multiplatform")
        id("dev.mokkery")
    }

    kotlin {
        jvm()
        js { nodejs() }
        @OptIn(org.jetbrains.kotlin.gradle.ExperimentalWasmDsl::class)
        wasmJs { nodejs() }
        when (HostManager.host) {
            is KonanTarget.LINUX_X64 -> linuxX64()
            is KonanTarget.LINUX_ARM64 -> linuxArm64()
            is KonanTarget.MACOS_ARM64 -> macosArm64()
            is KonanTarget.MINGW_X64 -> mingwX64()
            else -> error("Unsupported target ${'$'}{HostManager.host}")
        }
    }

    dependencies {
        commonTestImplementation(kotlin("test"))
        commonTestImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.10.2")
    }
"""

private fun hugeSource() = buildString {
    appendLine("package dev.mokkery.large")
    appendLine()
    appendInterface(name = "Huge", count = methodsCount, operator = "+")
    appendInterface(name = "TwinA", count = twinMethodsCount, operator = "+")
    appendInterface(name = "TwinB", count = twinMethodsCount, operator = "-")
}

private fun StringBuilder.appendInterface(name: String, count: Int, operator: String) {
    appendLine("interface $name {")
    repeat(count) {
        appendLine("    fun f$it(a: Int, b: String, c: Long): Int = a $operator $it")
        appendLine("    suspend fun s$it(a: Int, b: String, c: Long): Int = a $operator $it")
    }
    appendLine("}")
    appendLine()
}

private fun hugeTest() = buildString {
    appendLine(
        """
        package dev.mokkery.large

        import dev.mokkery.answering.calls
        import dev.mokkery.every
        import dev.mokkery.everySuspend
        import dev.mokkery.matcher.any
        import dev.mokkery.mock
        import dev.mokkery.mockMany
        import dev.mokkery.spy
        import dev.mokkery.t1
        import dev.mokkery.t2
        import dev.mokkery.verify
        import dev.mokkery.verifySuspend
        import kotlinx.coroutines.test.runTest
        import kotlin.test.Test
        import kotlin.test.assertEquals

        private val spied = object : Huge {}

        class HugeTest {
        """.trimIndent()
    )
    sampledMethods.forEach { i ->
        appendLine(
            """
            @Test
            fun callsOriginal$i() = runTest {
                val mock = mock<Huge>()
                every { mock.f$i(any(), any(), any()) } calls { callOriginal() }
                everySuspend { mock.s$i(any(), any(), any()) } calls { callOriginal() }
                assertEquals(${1 + i}, mock.f$i(1, "", 2L))
                assertEquals(${1 + i}, mock.s$i(1, "", 2L))
                verify { mock.f$i(1, "", 2L) }
                verifySuspend { mock.s$i(1, "", 2L) }
            }

            @Test
            fun callsSpied$i() = runTest {
                val spy = spy<Huge>(spied)
                assertEquals(${1 + i}, spy.f$i(1, "", 2L))
                assertEquals(${1 + i}, spy.s$i(1, "", 2L))
            }
            """.prependIndent("    ")
        )
    }
    sampledTwinMethods.forEach { i ->
        appendLine(
            """
            @Test
            fun callsSuperOfEachRootDeclaration$i() = runTest {
                val mock = mockMany<TwinA, TwinB>()
                every { mock.t1.f$i(any(), any(), any()) } calls { callSuper(TwinB::class) }
                everySuspend { mock.t2.s$i(any(), any(), any()) } calls { callSuper(TwinA::class) }
                assertEquals(${1 - i}, mock.t2.f$i(1, "", 2L))
                assertEquals(${1 + i}, mock.t1.s$i(1, "", 2L))
                verify { mock.t2.f$i(1, "", 2L) }
                verifySuspend { mock.t1.s$i(1, "", 2L) }
            }
            """.prependIndent("    ")
        )
    }
    appendLine("}")
}
