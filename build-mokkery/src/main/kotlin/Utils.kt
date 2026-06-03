
import org.gradle.api.Project
import org.gradle.kotlin.dsl.extra
import org.gradle.kotlin.dsl.withType
import org.jetbrains.kotlin.gradle.dsl.HasConfigurableKotlinCompilerOptions
import org.jetbrains.kotlin.gradle.dsl.KotlinVersion
import org.jetbrains.kotlin.gradle.plugin.KotlinSourceSetContainer
import org.jetbrains.kotlin.gradle.plugin.SubpluginOption
import org.jetbrains.kotlin.gradle.tasks.AbstractKotlinCompile
import org.jetbrains.kotlin.gradle.tasks.CompilerPluginOptions
import org.jetbrains.kotlin.gradle.tasks.KotlinNativeCompile
import java.net.URI
import java.util.*

const val testingRepoName = "testing"

val Project.testingRepoUrl: URI
    get() = rootProject
    .layout
    .buildDirectory
    .dir("testing-repository")
    .get()
    .asFile
    .toURI()

fun Project.loadLocalProperties() {
    val secretPropsFile = rootProject.file("local.properties")
    if (secretPropsFile.exists()) {
        secretPropsFile.reader().use {
            Properties().apply {
                load(it)
            }
        }.onEach { (name, value) ->
            extra[name.toString()] = value
        }
    }
}

fun Project.configureCompilerPlugin(id: String, vararg options: Pair<String, String>) {
    val compilerOptions = CompilerPluginOptions().apply {
        options.forEach { (name, value) ->
            addPluginArgument(id, SubpluginOption(name, value))
        }
    }
    tasks.withType<AbstractKotlinCompile<*>> {
        pluginOptions.add(compilerOptions)
    }
    tasks.withType<KotlinNativeCompile> {
        compilerPluginOptions.addPluginArgument(compilerOptions)
    }
}

fun KotlinSourceSetContainer.optInMokkeryDelicateAndInternals() {
    sourceSets.all {
        languageSettings.optIn("dev.mokkery.annotations.DelicateMokkeryApi")
        languageSettings.optIn("dev.mokkery.annotations.InternalMokkeryApi")
    }
}

fun HasConfigurableKotlinCompilerOptions<*>.setKotlinCompatibility(version: KotlinVersion) {
    compilerOptions {
        languageVersion.set(version)
        apiVersion.set(version)
    }
}
