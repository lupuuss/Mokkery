package dev.mokkery.gradle

import dev.mokkery.internal.MokkeryConfig
import org.intellij.lang.annotations.Language
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File

class MokkeryGradlePluginFunctionalTest {

    @field:TempDir
    private lateinit var projectDir: File

    @Test
    fun `test minimum Kotlin version`() {
        test(MokkeryConfig.MINIMUM_KOTLIN_VERSION)
    }

    private fun test(kotlinVersion: String) = gradleBuild(projectDir) {
        settings(
            kotlinPlugin = "multiplatform",
            projectName = "test-mokkery",
            kotlinVersion = kotlinVersion,
            extraKotlinPlugins = listOf("org.jetbrains.kotlin.plugin.allopen"),
        )
        file("build.gradle.kts", buildScript)
        copyRecursively(File("../test-mokkery/src"), "src")
        argument("--parallel")
    }.build("clean", "kotlinUpgradeYarnLock", "allTests")
}

@Language("kts")
private val buildScript = $$"""
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

    group = "dev.mokkery"

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
        
        jvm {
            compilerOptions {
                freeCompilerArgs.add("-Xemit-jvm-type-annotations")
            }
        }
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
        }
    }

    dependencies {
        commonTestImplementation(kotlin("test"))
        commonTestImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.9.0")
        commonTestImplementation(mokkery("coroutines"))
    }
""".trimIndent()
