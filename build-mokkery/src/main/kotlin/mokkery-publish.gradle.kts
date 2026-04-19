import MokkeryAttributes.GitConnectionUrl
import MokkeryAttributes.GitDevConnectionUrl
import MokkeryAttributes.GitHttpsUrl
import MokkeryAttributes.GitIssuesUrl
import MokkeryAttributes.WebsiteUrl
import com.vanniktech.maven.publish.JavadocJar
import com.vanniktech.maven.publish.SourcesJar
import java.net.URI

plugins {
    id("com.vanniktech.maven.publish")
    id("org.jetbrains.dokka")
}

publishing.repositories {
    maven {
        name = testingRepoName
        url = testingRepoUrl
    }
}

loadLocalProperties()

dokka.dokkaSourceSets.configureEach {
    sourceLink {
        localDirectory = rootDir
        remoteUrl = URI("${GitHttpsUrl}/tree/master")
        remoteLineSuffix = "#L"
    }
}

mavenPublishing {
    val isCentralPublishing = gradle.startParameter.taskNames.any { it.contains("MavenCentral") }
    coordinates(project.group.toString(), project.name, project.version.toString())
    // keeps signing, source jar and doc jar configured only for Maven Central
    if (isCentralPublishing) {
        signAllPublications()
    } else {
        @Suppress("UnstableApiUsage")
        configureBasedOnAppliedPlugins(sourcesJar = SourcesJar.None(), javadocJar = JavadocJar.None())
    }
    publishToMavenCentral(automaticRelease = false)
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
