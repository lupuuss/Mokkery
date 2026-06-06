import org.jetbrains.kotlin.gradle.dsl.KotlinVersion

plugins {
    id("mokkery-multiplatform")
    id("mokkery-publish")
}

kotlin {
    explicitApi()
    optInMokkeryDelicateAndInternals()
    setKotlinCompatibility(KotlinVersion.KOTLIN_2_2)
}

dependencies {
    commonMainCompileOnly(libs.kotlin.stdlib)
    commonTestImplementation(libs.kotlin.stdlib)
    commonTestImplementation(kotlin("test"))
}
