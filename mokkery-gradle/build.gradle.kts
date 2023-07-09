@file:Suppress("UnstableApiUsage")

plugins {
    kotlin("jvm")
    id("mokkery-base")
    id("com.gradle.plugin-publish") version "1.1.0"
}

dependencies {
    compileOnly(kotlin("gradle-plugin"))
    implementation(project(":mokkery-core"))
}

kotlin.sourceSets.all {
    languageSettings.optIn("dev.mokkery.annotations.InternalMokkeryApi")
}

val functionalTest by testing.suites.creating(JvmTestSuite::class) {
    val compilations = kotlin.target.compilations
    compilations.getByName("functionalTest").associateWith(compilations.getByName("main"))
    useJUnitJupiter()
    targets.configureEach {
        testTask.configure {
            listOf(":mokkery-runtime", ":mokkery-plugin", ":mokkery-core").forEach {
                dependsOn(project(it).tasks.named("publishToMavenLocal"))
            }
        }
    }
    dependencies {
        implementation(gradleTestKit())
    }
}

gradlePlugin {
    plugins {
        create(rootProject.name) {
            id = rootProject.extra["pluginId"] as String
            displayName = "Mokkery"
            description = "Hello mokkery!"
            version = project.version
            implementationClass = "${project.group}.gradle.MokkeryGradlePlugin"
            tags.set(listOf("kotlin", "mock"))
        }
    }
    testSourceSet(functionalTest.sources)
}
