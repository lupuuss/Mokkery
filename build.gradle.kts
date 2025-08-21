
import org.jetbrains.dokka.gradle.DokkaTask
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinProjectExtension
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

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
        extensions
            .findByType<KotlinProjectExtension>()
            ?.jvmToolchain(11)
        val javaVersion = JavaVersion.VERSION_1_8
        project
            .tasks
            .withType<JavaCompile>()
            .configureEach {
                sourceCompatibility = javaVersion.toString()
                targetCompatibility = javaVersion.toString()
            }
        project
            .tasks
            .withType<KotlinCompile>()
            .configureEach {
                compilerOptions {
                    jvmTarget.set(JvmTarget.fromTarget(javaVersion.toString()))
                }
            }
    }
}

dokka {
    moduleName.set("Mokkery")
    moduleVersion.set(rootProject.version.toString().replace("-SNAPSHOT", "", ignoreCase = true))
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
