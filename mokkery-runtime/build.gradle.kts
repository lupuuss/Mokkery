plugins {
    id("kotlinx-atomicfu")
    id("mokkery-multiplatform")
}

dependencies {
    commonMainApi(project(":mokkery-core"))
    commonTestImplementation(kotlin("test"))

    coroutinesMainCompileOnly(libs.kotlinx.coroutines.core)
    coroutinesTestImplementation(libs.kotlinx.coroutines.test)
}

rootProject.extensions.configure(org.jetbrains.kotlin.gradle.targets.js.nodejs.NodeJsRootExtension::class) {
    nodeVersion = "21.0.0-v8-canary202309167e82ab1fa2"
    nodeDownloadBaseUrl = "https://nodejs.org/download/v8-canary"
}

rootProject.tasks.withType(org.jetbrains.kotlin.gradle.targets.js.npm.tasks.KotlinNpmInstallTask::class) {
    args.add("--ignore-engines")
}
