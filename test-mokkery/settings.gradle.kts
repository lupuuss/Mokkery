pluginManagement {
    val mokkeryVersion: String by settings
    plugins {
        kotlin("multiplatform") version "1.8.21"
        id("dev.mokkery.plugin") version mokkeryVersion
    }
    val isTest: String by settings
    if (!isTest.toBoolean()) {
        repositories {
            mavenLocal()
            mavenCentral()
        }
    }
}

dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
        mavenLocal()
    }
}
