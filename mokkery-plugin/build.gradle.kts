plugins {
    id("mokkery-publish")
    kotlin("jvm")
    kotlin("kapt")
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
    kapt(libs.google.autoservice)
    compileOnly(libs.google.autoservice.annotations)
    compileOnly(libs.kotlin.compiler.embeddable)
    compileOnly(libs.kotlin.stdlib)
    implementation(project(":mokkery-core"))
}
