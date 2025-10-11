
import org.gradle.api.Project
import org.gradle.kotlin.dsl.extra
import org.gradle.kotlin.dsl.withType
import org.jetbrains.kotlin.gradle.plugin.SubpluginOption
import org.jetbrains.kotlin.gradle.tasks.AbstractKotlinCompile
import org.jetbrains.kotlin.gradle.tasks.CompilerPluginOptions
import org.jetbrains.kotlin.gradle.tasks.KotlinNativeCompile
import java.util.Properties

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

fun Project.configureCompilerPlugin(id: String, vararg options: SubpluginOption) {
    val compilerOptions = CompilerPluginOptions().apply {
        options.forEach {
            addPluginArgument(id, it)
        }
    }
    tasks.withType<AbstractKotlinCompile<*>> {
        pluginOptions.add(compilerOptions)
    }
    tasks.withType<KotlinNativeCompile> {
        compilerPluginOptions.addPluginArgument(compilerOptions)
    }
}
