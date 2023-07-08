pluginManagement {
    val mokkeryVersion: String by settings
    val kotlinVersion: String by settings
    plugins {
        kotlin("multiplatform") version kotlinVersion
        id("dev.mokkery") version mokkeryVersion
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
