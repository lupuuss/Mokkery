
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
        classpath(libs.gradle.plugin.kotlinx.atomicfu)
        classpath(libs.dokka.base)
    }
}

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
    moduleVersion.set(rootProject.version.toString())
    pluginConfiguration<DokkaBase, DokkaBaseConfiguration> {
        customAssets += rootProject.layout.projectDirectory.file("website/static/img/logo-icon.svg").asFile
    }
}
val syncDocs by tasks.registering(Sync::class) {
    group = "documentation"
    from(dokkaHtmlMultiModule.outputDirectory)
    into(rootProject.layout.projectDirectory.dir("website/static/api_reference"))
}
