@file:OptIn(InternalMokkeryApi::class)

package dev.mokkery.gradle

import dev.mokkery.annotations.InternalMokkeryApi
import dev.mokkery.internal.MokkeryConfig
import org.gradle.testfixtures.ProjectBuilder
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class MokkeryMockableGradlePluginTest : BaseMokkeryGradleTest() {

    @Test
    fun `mokkery mockable plugin adds mokkery-mockable-annotations to commonMainImplementation as ExternalDependency`() {
        val project = ProjectBuilder.builder().build()
        val version = MokkeryConfig.VERSION
        project.pluginManager.apply("org.jetbrains.kotlin.multiplatform")
        project.extensions.getByType(KotlinMultiplatformExtension::class.java).jvm()
        project.pluginManager.apply("dev.mokkery")
        project.fakeRepository(repoPath, "dev.mokkery:mokkery-gradle:$version")
        project.pluginManager.apply("dev.mokkery.mockable")
        project.evaluate()
        project.requireDependency("commonMainImplementation", "dev.mokkery:mokkery-mockable-annotations:$version")
    }

    @Test
    fun `mokkery mockable plugin fails when mokkery plugin is not applied`() {
        val project = ProjectBuilder.builder().build()
        project.pluginManager.apply("org.jetbrains.kotlin.multiplatform")
        project.extensions.getByType(KotlinMultiplatformExtension::class.java).jvm()
        val e = assertFailsWith<Exception> { project.pluginManager.apply("dev.mokkery.mockable") }
        assertEquals(
            expected = e.cause?.message,
            actual = "dev.mokkery.mockable plugin requires dev.mokkery plugin with the same version, but dev.mokkery plugin was not found!"
        )
    }

    @Test
    fun `mokkery mockable plugin fails when mokkery version does not match`() {
        val project = ProjectBuilder.builder().build()
        project.pluginManager.apply("org.jetbrains.kotlin.multiplatform")
        project.extensions.getByType(KotlinMultiplatformExtension::class.java).jvm()
        project.pluginManager.apply("dev.mokkery")
        project.fakeRepository(repoPath, "dev.mokkery:mokkery-gradle:0.0.1")
        val e = assertFailsWith<Exception> { project.pluginManager.apply("dev.mokkery.mockable") }
        assertEquals(
            expected = e.cause?.message,
            actual = "dev.mokkery.mockable plugin requires dev.mokkery with exact version! Versions: dev.mokkery=0.0.1 dev.mokkery.mockable=${MokkeryConfig.VERSION}"
        )
    }

}
