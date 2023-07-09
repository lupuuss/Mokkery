
buildscript {
    repositories {
        mavenCentral()
        google()
        maven {
            url = uri("https://plugins.gradle.org/m2/")
        }
    }

    dependencies {
        classpath(":build-mokkery")
        classpath(libs.gradle.plugin.kotlinx.atomicfu)
    }
}

rootProject.version = "1.0"
rootProject.group = "dev.mokkery"

val supportedKotlinVersions = listOf("1.8.22", "1.9.0")
if (libs.versions.kotlin.get() !in supportedKotlinVersions) error("Unsupported kotlin version!")

rootProject.ext["kotlinVersion"] = libs.versions.kotlin.get()
rootProject.ext["supportedKotlinVersions"] = supportedKotlinVersions
rootProject.ext["pluginVersion"] = "${libs.versions.kotlin.get()}-$version"
rootProject.ext["pluginId"] = "dev.mokkery"

allprojects {
    group = rootProject.group
    version = rootProject.version
    if (name in listOf("mokkery-plugin")) {
        version = rootProject.ext["pluginVersion"]!!
    }
    afterEvaluate {
        extensions.findByType<JavaPluginExtension>()?.apply {
            toolchain.languageVersion.set(JavaLanguageVersion.of(11))
        }
    }
}
