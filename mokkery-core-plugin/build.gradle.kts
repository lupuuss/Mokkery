plugins {
    kotlin("multiplatform")
    id("mokkery-base")
    id("org.jetbrains.kotlin.compiler.plugin.devkit.compiler-library")
    // no publishing - it's embedded into the plugin
}

kotlin {
    sourceSets.all {
        languageSettings.optIn("dev.mokkery.annotations.InternalMokkeryApi")
    }
    sourceSets.commonMain {
        kotlin.srcDir("src/main/kotlin")
        dependencies {
            implementation(project(":mokkery-core"))
            implementation(project(":mokkery-core-tooling"))
        }
    }
}
