plugins {
    id("mokkery-multiplatform")
    id("mokkery-publish")
}

kotlin {
    explicitApi()
}

dependencies {
    commonMainCompileOnly(libs.kotlin.stdlib)
}
