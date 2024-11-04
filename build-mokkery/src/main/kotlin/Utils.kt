
import org.gradle.api.Project
import org.gradle.kotlin.dsl.extra
import org.jetbrains.dokka.gradle.DokkaTaskPartial
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
