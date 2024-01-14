import MokkeryUrls.GitConnection
import MokkeryUrls.GitDevConnection
import MokkeryUrls.GitHttp
import MokkeryUrls.Website
import org.jetbrains.dokka.gradle.DokkaTaskPartial

plugins {
    id("org.jetbrains.dokka")
    kotlin("jvm")
    kotlin("kapt")
    `maven-publish`
    signing
}

signing {
    // disables signing for publishToMavenLocal
    setRequired { gradle.taskGraph.allTasks.any { it is PublishToMavenRepository } }
    sign(publishing.publications)
}

kotlin.sourceSets.all {
    languageSettings.apply {
        optIn("org.jetbrains.kotlin.compiler.plugin.ExperimentalCompilerApi")
        optIn("dev.mokkery.annotations.InternalMokkeryApi")
    }
}

tasks.named("dokkaGfm") { mustRunAfter("kaptKotlin") }
tasks.withType(DokkaTaskPartial::class) {
    enabled = false
}

dependencies {
    kapt(libs.google.autoservice)
    compileOnly(libs.google.autoservice.annotations)
    compileOnly(libs.kotlin.compiler.embeddable)
    implementation(project(":mokkery-core"))
}

loadLocalProperties()

java {
    withSourcesJar()
}

val dokkaJar by tasks.registering(Jar::class) {
    group = JavaBasePlugin.DOCUMENTATION_GROUP
    archiveClassifier.set("javadoc")
    from(tasks.getByName("dokkaGfm"))
}

publishing {

    repositories {
        maven {
            name = "sonatype"
            url = uri("https://s01.oss.sonatype.org/service/local/staging/deploy/maven2/")
            credentials {
                username = extraString("ossrhUsername")
                password = extraString("ossrhPassword")
            }
        }
    }
    publications.create<MavenPublication>("maven") {
        groupId = project.group as String
        artifactId = project.name
        version = project.version as String
        artifact(dokkaJar)
        from(components["java"])
        pom {
            name.set(project.name)
            description.set(
                "Kotlin compiler plugin for Mokkery - mocking library for Kotlin Multiplatform, easy to use, boilerplate-free and compiler plugin driven."
            )
            url.set(Website)
            licenses {
                license {
                    name.set("The Apache Software License, Version 2.0")
                    url.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
                    distribution.set("repo")
                }
            }
            developers {
                developer {
                    id.set("lupuuss")
                    name.set("lupuuss")
                }
            }
            scm {
                url.set(GitHttp)
                connection.set(GitConnection)
                developerConnection.set(GitDevConnection)
            }
        }
    }
}
