
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
        dokka(project(":mokkery-core-tooling"))
        dokka(project(":mokkery-runtime"))
        dokka(project(":mokkery-coroutines"))
        dokka(project(":mokkery-gradle"))
    }
}
