package dev.mokkery.gradle

import dev.mokkery.internal.MokkeryConfig
import org.gradle.testkit.runner.GradleRunner
import org.intellij.lang.annotations.Language
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File
import kotlin.io.path.ExperimentalPathApi
import kotlin.io.path.copyToRecursively

@OptIn(ExperimentalPathApi::class)
class MokkeryGradlePluginFunctionalTest {

    @field:TempDir
    private lateinit var testProjectDir: File

    private val buildFile by lazy { testProjectDir.resolve("build.gradle.kts") }
    private val settingsFile by lazy { testProjectDir.resolve("settings.gradle.kts") }

    @Test
    fun `test minimum Kotlin version`() {
        test(MokkeryConfig.MINIMUM_KOTLIN_VERSION)
    }

    @OptIn(ExperimentalPathApi::class)
    private fun test(kotlinVersion: String) {
        settingsFile.writeText(settingsFileContent)
        buildFile.writeText(buildFileContent)
        File("../test-mokkery/src").toPath()
            .copyToRecursively(
                testProjectDir
                    .resolve("src")
                    .toPath(),
                followLinks = false,
                overwrite = true,
            )
        GradleRunner.create()
            .withProjectDir(testProjectDir)
            .withArguments(
                "-PkotlinVersion=${kotlinVersion}",
                "-PmokkeryVersion=${MokkeryConfig.VERSION}",
                "-Porg.gradle.jvmargs=-Xmx1g",
                "-Pkotlin.daemon.jvmargs=-Xmx1g",
                "clean",
                "kotlinUpgradeYarnLock",
                "allTests",
                "--parallel",
            )
            .forwardOutput()
            .build()
    }
}

@Language("kts")
private val settingsFileContent = """
    pluginManagement {
        val kotlinVersion: String by settings
        val mokkeryVersion: String by settings
        plugins {
            kotlin("multiplatform") version kotlinVersion
            id("org.jetbrains.kotlin.plugin.allopen") version kotlinVersion
            id("dev.mokkery") version mokkeryVersion
        }
        repositories { 
            mavenCentral {
                content {
                    excludeGroup("dev.mokkery")
                }
            }
            maven {
                name = "testing"
                url = uri("${BuildConfig.TESTING_REPO_URL}")
            }
        }
    }

    dependencyResolutionManagement {
        repositories { 
            mavenCentral {
                content {
                    excludeGroup("dev.mokkery")
                }
            }
            maven {
                name = "testing"
                url = uri("${BuildConfig.TESTING_REPO_URL}")
            }
        }
    }
    """.trimIndent()

@Language("kts")
private val buildFileContent = $$"""
    @file:OptIn(ExperimentalKotlinGradlePluginApi::class, ExperimentalWasmDsl::class)

    import dev.mokkery.gradle.ApplicationRule
    import dev.mokkery.gradle.mokkery
    import dev.mokkery.options.AnnotationSelector.Companion.all
    import dev.mokkery.options.AnnotationSelector.Companion.named
    import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
    import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
    import org.jetbrains.kotlin.konan.target.HostManager
    import org.jetbrains.kotlin.konan.target.KonanTarget

    plugins {
        kotlin("multiplatform")
        id("dev.mokkery")
        id("org.jetbrains.kotlin.plugin.allopen")
    }

    allOpen {
        annotation("dev.mokkery.test.OpenForMokkery")
    }

    mokkery {
        ignoreFinalMembers = true
        rule = ApplicationRule.All
        stubs.allowConcreteClassInstantiation = true
        stubs.allowClassInheritance = true
        annotations.copyToMock = all - named("dev.mokkery.test.AnnotationB", "dev.mokkery.test.AnnotationC")
    }

    kotlin {
        applyDefaultHierarchyTemplate {
            common {
                group("wasm") {
                    withWasmJs()
                }
            }
        }
        
        jvm()
        js(IR) { nodejs() }
        wasmJs { nodejs() }
        
        when (HostManager.host) {
            is KonanTarget.LINUX_X64 -> linuxX64()
            is KonanTarget.LINUX_ARM64 -> linuxArm64()
            is KonanTarget.MACOS_X64 -> macosX64()
            is KonanTarget.MACOS_ARM64 -> {
                 macosArm64()
                 iosSimulatorArm64()
            }
            is KonanTarget.MINGW_X64 -> mingwX64()
            else -> error("Unsupported target ${HostManager.host}")
        }

        @OptIn(ExperimentalKotlinGradlePluginApi::class)
        compilerOptions {
            freeCompilerArgs.add("-Xexpect-actual-classes")
            freeCompilerArgs.add("-Xcontext-parameters")
        }
    }

    dependencies {
        commonTestImplementation(kotlin("test"))
        commonTestImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.9.0")
        commonTestImplementation(mokkery("coroutines"))
    }
""".trimIndent()
