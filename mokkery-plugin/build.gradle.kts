plugins {
    kotlin("jvm")
    kotlin("kapt")
    id("mokkery-publish")
}

kotlin.sourceSets.all {
    languageSettings.apply {
        optIn("org.jetbrains.kotlin.compiler.plugin.ExperimentalCompilerApi")
        optIn("org.jetbrains.kotlin.ir.symbols.UnsafeDuringIrConstructionAPI")
        optIn("dev.mokkery.annotations.InternalMokkeryApi")
    }
}

val embedded by configurations.registering { isTransitive = false }

configurations.compileOnly { extendsFrom(embedded.get()) }

dependencies {
    kapt(libs.google.autoservice)
    compileOnly(libs.google.autoservice.annotations)
    compileOnly(libs.kotlin.compiler)
    compileOnly(libs.kotlin.stdlib)
    embedded(project(":mokkery-core"))
    embedded(project(":mokkery-core-tooling"))
    embedded(project(":mokkery-core-plugin"))
}

tasks.jar {
    from(embedded.get().map(::zipTree))
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
}
