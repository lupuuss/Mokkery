plugins {
    id("kotlinx-atomicfu")
    id("mokkery-multiplatform")
}

kotlin.sourceSets.all {
    languageSettings.optIn("dev.mokkery.annotations.DelicateMokkeryApi")
}

dependencies {
    commonMainApi(project(":mokkery-setup"))
    commonMainImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.1")
}
