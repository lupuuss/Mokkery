plugins {
    kotlin("jvm")
    id("mokkery-base")
    id("com.gradle.plugin-publish") version "1.1.0"
}

dependencies {
    implementation(kotlin("gradle-plugin"))
}

gradlePlugin {
    plugins {
        create(rootProject.name) {
            id = project.ext["pluginId"] as String
            displayName = "Mokkery"
            description = "Hello mokkery!"
            version = "1.0"
            implementationClass = "${project.group}.gradle.MokkeryGradlePlugin"
            tags.set(listOf("kotlin", "mock"))
        }
    }
}
