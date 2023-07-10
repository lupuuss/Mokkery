import MokkeryUrls.GitConnection
import MokkeryUrls.GitDevConnection
import MokkeryUrls.GitHttp
import MokkeryUrls.Website

plugins {
    `maven-publish`
    signing
    id("org.jetbrains.dokka")
}

loadLocalProperties()

signing {
    // disables signing for publishToMavenLocal
    setRequired { gradle.taskGraph.allTasks.any { it is PublishToMavenRepository } }
    sign(publishing.publications)
}

val dokkaJar by tasks.registering( Jar::class) {
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
    publications.withType<MavenPublication> {
        artifact(dokkaJar)
        pom {
            name.set(project.name)
            description.set(
                "Mokkery is a mocking library for Kotlin Multiplatform, easy to use, boilerplate-free and compiler plugin driven."
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
