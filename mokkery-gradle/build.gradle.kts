@file:Suppress("UnstableApiUsage")

plugins {
    kotlin("jvm")
    id("com.gradle.plugin-publish") version "1.1.0"
}

dependencies {
    compileOnly(kotlin("gradle-plugin"))
    api(project(":mokkery-core"))
}

kotlin {
    explicitApi()
    sourceSets.all {
        languageSettings.optIn("dev.mokkery.annotations.InternalMokkeryApi")
    }
}

val functionalTest by testing.suites.creating(JvmTestSuite::class) {
    val compilations = kotlin.target.compilations
    compilations.getByName("functionalTest").associateWith(compilations.getByName("main"))
    useJUnitJupiter()
    targets.configureEach {
        testTask.configure {
            mustRunAfter("test")
            listOf(":mokkery-runtime", ":mokkery-plugin", ":mokkery-core", ":mokkery-gradle").forEach {
                dependsOn(project(it).tasks.named("publishToMavenLocal"))
            }
        }
    }
    dependencies {
        implementation(gradleTestKit())
    }
}

tasks.check { dependsOn(functionalTest) }

gradlePlugin {
    website.set(MokkeryUrls.Website)
    vcsUrl.set("${MokkeryUrls.GitHttp}.git")
    plugins {
        create(rootProject.name) {
            id = rootProject.extra["pluginId"] as String
            displayName = "Mokkery"
            description =
                "Gradle plugin for Mokkery - mocking library for Kotlin Multiplatform, easy to use, boilerplate-free and compiler plugin driven."
            version = project.version
            implementationClass = "${project.group}.gradle.MokkeryGradlePlugin"
            tags.set(
                listOf(
                    "kotlin",
                    "mock",
                    "test",
                    "kotlin-multiplatform",
                    "kotlin-multiplatform-mobile",
                    "kotlin-compiler-plugin"
                )
            )
        }
    }
    testSourceSet(functionalTest.sources)
}
