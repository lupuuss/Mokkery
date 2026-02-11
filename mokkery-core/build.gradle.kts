plugins {
    id("mokkery-publish")
    id("mokkery-multiplatform")
}

kotlin {
    explicitApi()
    optInMokkeryDelicateAndInternals()
}

dependencies {
    commonMainCompileOnly(libs.kotlin.stdlib)

    jsMainCompileOnly(libs.kotlin.dom.api.compat)
}
