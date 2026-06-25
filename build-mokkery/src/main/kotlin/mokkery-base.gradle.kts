import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinProjectExtension
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

val javaVersion = JavaVersion.VERSION_1_8

tasks.withType<JavaCompile>().configureEach {
    sourceCompatibility = javaVersion.toString()
    targetCompatibility = javaVersion.toString()
}
tasks.withType<KotlinCompile>().configureEach {
    compilerOptions {
        jvmTarget.set(JvmTarget.fromTarget(javaVersion.toString()))
        freeCompilerArgs.add("-Xjdk-release=$javaVersion")
    }
}

listOf("org.jetbrains.kotlin.multiplatform", "org.jetbrains.kotlin.jvm").forEach { pluginId ->
    plugins.withId(pluginId) {
        extensions.getByType<KotlinProjectExtension>().jvmToolchain(17)
    }
}
