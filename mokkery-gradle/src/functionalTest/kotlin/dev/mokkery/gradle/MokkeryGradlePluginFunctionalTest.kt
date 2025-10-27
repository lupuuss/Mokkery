package dev.mokkery.gradle

import dev.mokkery.MokkeryConfig
import org.gradle.testkit.runner.GradleRunner
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File
import kotlin.io.path.ExperimentalPathApi
import kotlin.io.path.copyToRecursively

class MokkeryGradlePluginFunctionalTest {

    @field:TempDir
    private lateinit var testProjectDir: File

    private val buildFile by lazy { testProjectDir.resolve("build.gradle.kts") }
    private val settingsFile by lazy { testProjectDir.resolve("settings.gradle.kts") }
    private val localPropertiesFile by lazy { testProjectDir.resolve("local.properties") }

    @Test
    fun `test minimum Kotlin version`() {
        test(MokkeryConfig.MINIMUM_KOTLIN_VERSION)
    }

    @OptIn(ExperimentalPathApi::class)
    private fun test(kotlinVersion: String) {
        settingsFile.writeText(settingsFileContent)
        buildFile.writeText(buildFileContent)
        File("../local.properties")
            .takeIf { it.exists() }
            ?.copyTo(localPropertiesFile)

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
            )
            .forwardOutput()
            .build()
    }
}

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
            mavenCentral  {
                content {
                    excludeGroup("dev.mokkery")
                }
            }
            google {
                content {
                    excludeGroup("dev.mokkery")
                }
            }
            mavenLocal()
        }
    }

    dependencyResolutionManagement {
        repositories {
            mavenCentral {
                content {
                    excludeGroup("dev.mokkery")
                }
            }
            google {
                content {
                    excludeGroup("dev.mokkery")
                }
            }
            mavenLocal()
        }
    }
    """.trimIndent()
private val buildFileContent = $$"""
    @file:OptIn(ExperimentalKotlinGradlePluginApi::class, ExperimentalWasmDsl::class)

    import dev.mokkery.gradle.ApplicationRule
    import dev.mokkery.gradle.mokkery
    import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
    import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
    import org.jetbrains.kotlin.konan.target.HostManager
    import org.jetbrains.kotlin.konan.target.KonanTarget

    buildscript {
        dependencies {
            classpath("com.android.tools.build:gradle:8.11.0")
        }
    }

    plugins {
        kotlin("multiplatform")
        id("dev.mokkery")
        id("org.jetbrains.kotlin.plugin.allopen")
        id("com.android.library") version "8.11.0"
    }

    android {
        namespace = "dev.mokkery.test"
        compileSdk = 36
    }

    allOpen {
        annotation("dev.mokkery.test.OpenForMokkery")
    }

    mokkery {
        ignoreFinalMembers = true
        rule = ApplicationRule.All
    }

    kotlin {
        applyDefaultHierarchyTemplate {
            common {
                group("wasm") {
                    withWasmJs()
                }
            }
        }
        
        androidTarget()
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
