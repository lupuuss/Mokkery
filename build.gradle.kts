
import org.jetbrains.dokka.gradle.DokkaMultiModuleTask

plugins {
    alias(libs.plugins.dokka)
}

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

val kotlinVersion = libs.versions.kotlin.get()
val mokkeryVersion = "1.6.0"
rootProject.ext["kotlinVersion"] = kotlinVersion
rootProject.version = "${kotlinVersion}-${mokkeryVersion}"
rootProject.group = "dev.mokkery"

rootProject.ext["pluginId"] = "dev.mokkery"

allprojects {
    group = rootProject.group
    version = rootProject.version
    afterEvaluate {
        extensions.findByType<JavaPluginExtension>()?.apply {
            toolchain.languageVersion.set(JavaLanguageVersion.of(8))
        }
    }
}

val dokkaHtmlMultiModule by tasks.getting(DokkaMultiModuleTask::class) {
    moduleName.set("Mokkery")
    moduleVersion.set(mokkeryVersion)
}
val syncDocs by tasks.registering(Sync::class) {
    group = "documentation"
    from(dokkaHtmlMultiModule.outputDirectory)
    into(rootProject.layout.projectDirectory.dir("docs"))
}
