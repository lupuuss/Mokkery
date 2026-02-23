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
    jsMainCompileOnly(libs.kotlin.dom.api.compat)

    commonTestImplementation(libs.kotlin.stdlib)
    commonTestImplementation(kotlin("test"))
}
