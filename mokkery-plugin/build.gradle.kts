plugins {
    kotlin("multiplatform")
    id("mokkery-base")
    id("org.jetbrains.kotlin.compiler.plugin.devkit.compiler-plugin")
    id("mokkery-publish")
}

pluginDevKit {
    componentRegistrar = "dev.mokkery.plugin.MokkeryCompilerPluginRegistrar"
    commandLineProcessor = "dev.mokkery.plugin.MokkeryCommandLineProcessor"
    companionLibrary(project(":mokkery-runtime"))
    versionHierarchy {
        splitDev(2, 5)
    }
}

configurations.matching { it.name.endsWith("TestRuntimeClasspath") }.configureEach {
    resolutionStrategy.dependencySubstitution {
        substitute(module("org.jetbrains.kotlinx:kotlinx-coroutines-core-jvm"))
            .using(module(libs.intellij.kotlinx.coroutines.core.jvm.get().toString()))
    }
}

kotlin {
    sourceSets.matching { !it.name.contains("EntryPoint", ignoreCase = true) }.all {
        languageSettings.optIn("dev.mokkery.annotations.InternalMokkeryApi")
    }
    sourceSets.commonMain {
        kotlin.srcDir("src/main/kotlin")
        dependencies {
            implementation(project(":mokkery-core"))
            implementation(project(":mokkery-core-tooling"))
            implementation(project(":mokkery-core-plugin"))
        }
    }
}
