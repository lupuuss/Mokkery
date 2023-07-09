plugins {
    `kotlin-dsl`
}

dependencies {
    api(libs.kotlin.plugin)
    api(libs.buildconfig.plugin)
    api(libs.dokka.plugin)
}

java.toolchain.languageVersion.set(JavaLanguageVersion.of(8))
