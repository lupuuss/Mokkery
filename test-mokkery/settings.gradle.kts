pluginManagement {
    val mokkeryVersion: String by settings
    val kotlinVersion: String by settings
    plugins {
        kotlin("multiplatform") version kotlinVersion
        id("org.jetbrains.kotlin.plugin.allopen") version kotlinVersion
        id("dev.mokkery") version mokkeryVersion
    }
    repositories {
        mavenLocal()
        mavenCentral()
        // TODO REMOVE after Poko release
        maven {
            url = uri("https://oss.sonatype.org/content/repositories/snapshots/")
        }
    }
}

dependencyResolutionManagement {
    repositories {
        mavenLocal()
        mavenCentral()
        // TODO REMOVE after Poko release
        maven {
            url = uri("https://oss.sonatype.org/content/repositories/snapshots/")
        }
    }
}
