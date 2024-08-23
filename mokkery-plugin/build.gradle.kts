import org.jetbrains.dokka.gradle.DokkaTaskPartial

plugins {
    id("mokkery-publish")
    kotlin("jvm")
    kotlin("kapt")
}


kotlin.sourceSets.all {
    languageSettings.apply {
        optIn("org.jetbrains.kotlin.compiler.plugin.ExperimentalCompilerApi")
        optIn("org.jetbrains.kotlin.ir.symbols.UnsafeDuringIrConstructionAPI")
        optIn("dev.mokkery.annotations.InternalMokkeryApi")
    }
}

tasks.withType(DokkaTaskPartial::class) {
    enabled = false
}

dependencies {
    kapt(libs.google.autoservice)
    compileOnly(libs.google.autoservice.annotations)
    compileOnly(libs.kotlin.compiler.embeddable)
    implementation(project(":mokkery-core"))
}
