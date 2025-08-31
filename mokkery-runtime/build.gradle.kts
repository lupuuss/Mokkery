plugins {
    id("mokkery-publish")
    id("mokkery-multiplatform")
    alias(libs.plugins.poko)
    alias(libs.plugins.kotlinx.atomicfu)
    alias(libs.plugins.agp.library)
}

kotlin {
    androidTarget {
        publishLibraryVariants("release")
    }
}

android {
    compileSdk = 36
    namespace = rootProject.group.toString()
}

dependencies {
    commonMainApi(project(":mokkery-core"))
    commonMainCompileOnly(libs.kotlin.stdlib)

    jsMainCompileOnly(libs.kotlin.dom.api.compat)

    jvmMainImplementation(libs.objenesis)
    jvmMainImplementation(libs.bytebuddy)

    val androidMainImplementation by configurations.getting
    androidMainImplementation(libs.kotlin.stdlib)
    androidMainImplementation(libs.objenesis)
    androidMainImplementation(libs.bytebuddy)
    androidMainImplementation(libs.dexmaker)
    // workaround for https://github.com/Kotlin/kotlinx-atomicfu/issues/145
    androidMainImplementation("org.jetbrains.kotlinx:atomicfu:${libs.versions.atomicfu.get()}")

    commonTestImplementation(kotlin("test"))
}
