import MokkeryUrls.GitConnection
import MokkeryUrls.GitDevConnection
import MokkeryUrls.GitHttp
import MokkeryUrls.Website

plugins {
    `maven-publish`
    signing
}

loadLocalProperties()


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
        val publication = this
        signing {
            // disables signing for publishToMavenLocal
            setRequired { gradle.taskGraph.allTasks.any { it is PublishToMavenRepository } }
            sign(publication)
        }
        val dokkaJar = tasks.register("${publication.name}DokkaJar", Jar::class) {
            group = JavaBasePlugin.DOCUMENTATION_GROUP
            archiveClassifier.set("javadoc")
            archiveBaseName.set("${archiveBaseName.get()}-${publication.name}")
            from(tasks.findByName("dokkaGfm"))
        }
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
