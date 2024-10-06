import MokkeryAttributes.GitConnectionUrl
import MokkeryAttributes.GitDevConnectionUrl
import MokkeryAttributes.GitHttpsUrl
import MokkeryAttributes.GitIssuesUrl
import MokkeryAttributes.WebsiteUrl
import com.vanniktech.maven.publish.SonatypeHost

plugins {
    id("com.vanniktech.maven.publish")
    id("org.jetbrains.dokka")
}

loadLocalProperties()

mavenPublishing {
    coordinates(project.group.toString(), project.name, project.version.toString())
    signAllPublications()
    publishToMavenCentral(SonatypeHost.S01, automaticRelease = false)
    pom {
        name.set(project.name)
        description.set(MokkeryAttributes.Description)
        url.set(WebsiteUrl)
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
            url.set(GitHttpsUrl)
            connection.set(GitConnectionUrl)
            developerConnection.set(GitDevConnectionUrl)
        }
        issueManagement {
            url.set(GitIssuesUrl)
        }
    }
}
