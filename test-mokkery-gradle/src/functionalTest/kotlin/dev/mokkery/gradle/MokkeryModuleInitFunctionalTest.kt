package dev.mokkery.gradle

import org.intellij.lang.annotations.Language
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File

class MokkeryModuleInitFunctionalTest {

    @field:TempDir
    private lateinit var projectDir: File

    @Test
    fun `reports failing initializer and does not rerun it on next access`() {
        val build = gradleBuild(projectDir) {
            settings(kotlinPlugin = "jvm", projectName = "module-init")
            file("build.gradle.kts", buildScript)
            file("src/test/kotlin/dev/mokkery/init/ModuleInitTest.kt", moduleInitTest)
        }
        build.build(":test")
    }
}

@Language("kts")
private val buildScript = """
    plugins {
        kotlin("jvm")
        id("dev.mokkery")
    }

    group = "dev.mokkery"

    dependencies {
        testImplementation(kotlin("test"))
    }

    tasks.test {
        useJUnitPlatform()
    }
"""

private val moduleInitTest = """
    package dev.mokkery.init

    import dev.mokkery.MokkeryRuntimeException
    import dev.mokkery.MokkeryScope
    import dev.mokkery.annotations.MokkeryModuleInit
    import kotlin.test.Test
    import kotlin.test.assertEquals
    import kotlin.test.assertFailsWith
    import kotlin.test.assertSame

    private var attempts = 0

    @MokkeryModuleInit
    fun accessModule() {
        attempts++
        MokkeryScope.module
    }

    class ModuleInitTest {

        @Test
        fun reportsFailingInitializerAndDoesNotRerunItOnNextAccess() {
            val error = assertFailsWith<MokkeryRuntimeException> { MokkeryScope.module }
            assertEquals(
                "Mokkery initializer function 'dev.mokkery.init.accessModule' failed in module 'dev.mokkery:module-init_test'.",
                error.message,
            )
            assertEquals(
                "`MokkeryScope.module` is not available while module initializers run! Accept `MokkeryScope` as the initializer parameter instead.",
                error.cause?.message,
            )
            val nextError = assertFailsWith<MokkeryRuntimeException> { MokkeryScope.module }
            assertEquals("`MokkeryScope.module` initialization failed earlier!", nextError.message)
            assertSame(error, nextError.cause)
            assertEquals(1, attempts)
        }
    }
"""
