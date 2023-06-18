plugins {
    id("mokkery-base")
    kotlin("jvm")
    kotlin("kapt")
}

kotlin.sourceSets.all {
    languageSettings.apply {
        optIn("org.jetbrains.kotlin.compiler.plugin.ExperimentalCompilerApi")
        optIn("kotlin.io.path.ExperimentalPathApi")
    }
}

dependencies {
    kapt(libs.google.autoservice)
    compileOnly(libs.google.autoservice.annotations)
    compileOnly(libs.kotlin.compiler.embeddable)
    implementation(project(":mokkery-core"))
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            groupId = project.group as String
            artifactId = project.name
            version = project.version as String

            from(components["java"])
        }
    }
}
