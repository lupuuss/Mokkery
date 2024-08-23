import MokkeryUrls.GitConnection
import MokkeryUrls.GitDevConnection
import MokkeryUrls.GitHttp
import MokkeryUrls.Website
import com.vanniktech.maven.publish.SonatypeHost

plugins {
    id("com.vanniktech.maven.publish")
    id("org.jetbrains.dokka")
}

mavenPublishing {
    coordinates(project.group.toString(), project.name, project.version.toString())
    signAllPublications()
    publishToMavenCentral(SonatypeHost.S01, automaticRelease = false)
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