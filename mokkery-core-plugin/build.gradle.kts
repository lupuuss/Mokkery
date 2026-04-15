plugins {
    kotlin("jvm")
    // no publishing - it's embedded into the plugin
}

kotlin.compilerOptions.freeCompilerArgs.add("-Xcontext-parameters")

kotlin.sourceSets.all {
    languageSettings.apply {
        optIn("org.jetbrains.kotlin.compiler.plugin.ExperimentalCompilerApi")
        optIn("org.jetbrains.kotlin.ir.symbols.UnsafeDuringIrConstructionAPI")
        optIn("dev.mokkery.annotations.InternalMokkeryApi")
    }
}

dependencies {
    compileOnly(libs.kotlin.compiler)
    compileOnly(libs.kotlin.stdlib)
    compileOnly(project(":mokkery-core"))
    compileOnly(project(":mokkery-core-tooling"))
}
