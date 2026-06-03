package dev.mokkery.gradle

import dev.mokkery.internal.MokkeryConfig
import org.gradle.testfixtures.ProjectBuilder
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class MokkeryGradlePluginTest : BaseMokkeryGradleTest() {

    @Test
    fun `mokkery plugin fails when kotlin plugin is not applied`() {
        val project = ProjectBuilder.builder().build()
        val e = assertFailsWith<Exception> { project.pluginManager.apply("dev.mokkery") }
        assertEquals(e.cause?.message.orEmpty(), "Kotlin plugin not applied! Mokkery requires kotlin plugin!")
    }

    @Test
    fun `mokkery plugin adds mokkery-runtime to commonTestImplementation as ExternalDependency`() {
        val project = ProjectBuilder.builder().build()
        project.pluginManager.apply("org.jetbrains.kotlin.multiplatform")
        project.extensions.getByType(KotlinMultiplatformExtension::class.java).jvm()
        project.pluginManager.apply("dev.mokkery")
        project.evaluate()
        project.requireDependency("commonTestImplementation", "dev.mokkery:mokkery-runtime:${MokkeryConfig.VERSION}")
    }
}
