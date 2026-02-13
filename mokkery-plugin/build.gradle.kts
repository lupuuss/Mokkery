plugins {
    kotlin("jvm")
    kotlin("kapt")
    id("mokkery-publish")
}

kotlin.compilerOptions.freeCompilerArgs.add("-Xcontext-parameters")

kotlin.sourceSets.all {
    languageSettings.apply {
        optIn("org.jetbrains.kotlin.compiler.plugin.ExperimentalCompilerApi")
        optIn("org.jetbrains.kotlin.ir.symbols.UnsafeDuringIrConstructionAPI")
        optIn("dev.mokkery.annotations.InternalMokkeryApi")
    }
}

val embeddedClasspath: Configuration by configurations.creating { isTransitive = false }

dependencies {
    kapt(libs.google.autoservice)
    compileOnly(libs.google.autoservice.annotations)
    compileOnly(libs.kotlin.compiler.embeddable)
    compileOnly(libs.kotlin.stdlib)
    embedded(project(":mokkery-core"))
    embedded(project(":mokkery-core-tooling"))
}

fun DependencyHandlerScope.embedded(dependency: Any) {
    compileOnly(dependency)
    embeddedClasspath(dependency)
}

tasks.jar {
    from(embeddedClasspath.map(::zipTree))
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
}
