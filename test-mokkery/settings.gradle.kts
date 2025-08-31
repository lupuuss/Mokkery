pluginManagement {
    val mokkeryVersion: String by settings
    val kotlinVersion: String by settings
    plugins {
        kotlin("multiplatform") version kotlinVersion
        id("org.jetbrains.kotlin.plugin.allopen") version kotlinVersion
        id("dev.mokkery") version mokkeryVersion
    }
    repositories {
        mavenCentral  {
            content {
                excludeGroup("dev.mokkery")
            }
        }
        google {
            content {
                excludeGroup("dev.mokkery")
            }
        }
        mavenLocal()
    }
}

dependencyResolutionManagement {
    repositories {
        mavenCentral {
            content {
                excludeGroup("dev.mokkery")
            }
        }
        google {
            content {
                excludeGroup("dev.mokkery")
            }
        }
        mavenLocal()
    }
}
