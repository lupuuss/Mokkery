@file:Suppress("UnstableApiUsage")

plugins {
    kotlin("jvm")
    id("mokkery-publish")
    alias(libs.plugins.gradle.portal.publish)
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
            listOf(
                ":mokkery-runtime",
                ":mokkery-plugin",
                ":mokkery-core",
                ":mokkery-gradle",
                ":mokkery-coroutines"
            ).forEach {
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
    website.set(MokkeryAttributes.WebsiteUrl)
    vcsUrl.set(MokkeryAttributes.GitVscUrl)
    plugins {
        create(rootProject.name) {
            id = rootProject.extra["pluginId"] as String
            displayName = MokkeryAttributes.DisplayName
            description = MokkeryAttributes.Description
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
