
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

allprojects {
    group = "dev.mokkery"
    version = "1.0"
    ext["pluginId"] = "dev.mokkery.plugin"

    afterEvaluate {
        extensions.findByType<JavaPluginExtension>()?.apply {
            toolchain.languageVersion.set(JavaLanguageVersion.of(11))
        }
    }
}
