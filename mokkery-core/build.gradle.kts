plugins {
    alias(libs.plugins.kotlin.multiplatform)
    `maven-publish`
}

kotlin {
    explicitApi()
    jvm()
    js(IR) {}
    mingwX64()
}
