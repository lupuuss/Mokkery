@file:Suppress("UnstableApiUsage")

plugins {
    kotlin("jvm")
    id("mokkery-base")
    id("com.gradle.plugin-publish") version "1.1.0"
}

dependencies {
    implementation(kotlin("gradle-plugin"))
    implementation(project(":mokkery-core"))
}

kotlin.sourceSets.all {
    languageSettings.optIn("dev.mokkery.annotations.InternalMokkeryApi")
}

val functionalTest by testing.suites.creating(JvmTestSuite::class) {
    useJUnitJupiter()
    dependencies {
        implementation(gradleTestKit())
    }
}

functionalTest.targets.configureEach {
    testTask.configure {
        listOf(":mokkery-runtime", ":mokkery-plugin", ":mokkery-core").forEach {
            dependsOn( project(it).tasks.named("publishToMavenLocal"))
        }
    }
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
    testSourceSet(functionalTest.sources)
}
