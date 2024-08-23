plugins {
    `kotlin-dsl`
}

dependencies {
    api(libs.kotlin.plugin)
    api(libs.buildconfig.plugin)
    api(libs.dokka.plugin)
    api(libs.vanniktech.publish.plugin)
}

java.toolchain.languageVersion.set(JavaLanguageVersion.of(11))
