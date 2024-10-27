
import org.jetbrains.dokka.base.DokkaBase
import org.jetbrains.dokka.base.DokkaBaseConfiguration
import org.jetbrains.dokka.gradle.DokkaMultiModuleTask
import org.jetbrains.dokka.gradle.DokkaTask

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
        classpath(libs.dokka.base)
    }
}

rootProject.group = "dev.mokkery"

rootProject.ext["pluginId"] = "dev.mokkery"

allprojects {
    group = rootProject.group
    version = rootProject.version
    tasks.withType<DokkaTask> {
        onlyIf { "SNAPSHOT" !in version.toString() }
    }
    afterEvaluate {
        extensions.findByType<JavaPluginExtension>()?.apply {
            toolchain.languageVersion.set(JavaLanguageVersion.of(8))
        }
    }
}

dokka {
    moduleName.set("Mokkery")
    moduleVersion.set(rootProject.version.toString())
    pluginsConfiguration.html {
        customAssets.from(rootProject.layout.projectDirectory.file("website/static/img/logo-icon.svg").asFile)
    }
    dokkaPublications.html {
        outputDirectory.set(rootProject.layout.projectDirectory.dir("website/static/api_reference"))
    }
    dependencies {
        dokka(project(":mokkery-core"))
        dokka(project(":mokkery-runtime"))
        dokka(project(":mokkery-coroutines"))
        dokka(project(":mokkery-gradle"))
    }
}
