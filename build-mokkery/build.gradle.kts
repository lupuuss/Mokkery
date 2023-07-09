plugins {
    `kotlin-dsl`
}

dependencies {
    api(libs.kotlin.plugin)
    api(libs.buildconfig.plugin)
}

java.toolchain.languageVersion.set(JavaLanguageVersion.of(8))
