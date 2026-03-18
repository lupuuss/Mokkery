plugins {
    id("mokkery-multiplatform")
    id("mokkery-publish")
}

kotlin {
    explicitApi()
    optInMokkeryDelicateAndInternals()
}

dependencies {
    commonMainCompileOnly(libs.kotlin.stdlib)
    commonTestImplementation(libs.kotlin.stdlib)
    commonTestImplementation(kotlin("test"))
}
