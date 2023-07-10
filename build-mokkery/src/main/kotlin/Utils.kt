
import org.gradle.api.Project
import org.gradle.kotlin.dsl.extra
import java.util.*

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

fun Project.extraString(name: String) = if (extra.has(name)) extra.get(name) as String else null
