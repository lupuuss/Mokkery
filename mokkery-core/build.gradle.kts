plugins {
    alias(libs.plugins.kotlin.multiplatform)
    `maven-publish`
    id("kotlinx-atomicfu")
}

kotlin {
    explicitApi()
    jvm()
    js(IR) {}
    mingwX64()
}
