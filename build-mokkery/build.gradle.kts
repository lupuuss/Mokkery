plugins {
    `kotlin-dsl`
}

dependencies {
    api(libs.kotlin.plugin)
    api(libs.buildconfig.plugin)
    api(libs.dokka.plugin)
    api(libs.vanniktech.publish.plugin)
    api(libs.devkit.plugins)
}

java.toolchain.languageVersion.set(JavaLanguageVersion.of(17))
