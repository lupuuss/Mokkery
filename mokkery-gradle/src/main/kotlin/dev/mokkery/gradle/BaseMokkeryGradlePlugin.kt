package dev.mokkery.gradle

import dev.mokkery.annotations.InternalMokkeryApi
import dev.mokkery.internal.options.MokkeryOptionProjection
import dev.mokkery.internal.options.MokkeryOptionsContainer
import org.gradle.api.Project
import org.gradle.api.provider.Provider
import org.jetbrains.kotlin.gradle.dsl.kotlinExtension
import org.jetbrains.kotlin.gradle.plugin.KotlinCompilation
import org.jetbrains.kotlin.gradle.plugin.KotlinCompilerPluginSupportPlugin
import org.jetbrains.kotlin.gradle.plugin.KotlinSourceSet
import org.jetbrains.kotlin.gradle.plugin.SubpluginOption

@InternalMokkeryApi
public abstract class BaseMokkeryGradlePlugin: KotlinCompilerPluginSupportPlugin {

    public abstract val options: MokkeryOptionsContainer

    public abstract fun optionProjection(project: Project): MokkeryOptionProjection<MokkeryGradleProperty<Any>>

    public abstract fun rule(project: Project): ApplicationRule

    public fun sourceSetsForDependencies(
        project: Project,
    ): List<KotlinSourceSet> {
        val rule = rule(project)
        val applicableSourceSets = project.kotlinExtension
            .sourceSets
            .filter { rule.isApplicable(it) }
            .toSet()
        return applicableSourceSets
            .filter { sourceSet -> sourceSet.dependsOn.none { it in applicableSourceSets } }
    }

    public fun applyOptionConventions(project: Project) {
        val projection = optionProjection(project)
        options.forEach {
            projection.project(it).convention(it.defaultValues)
        }
    }

    override fun applyToCompilation(kotlinCompilation: KotlinCompilation<*>): Provider<List<SubpluginOption>> {
        val project = kotlinCompilation.target.project
        val projection = optionProjection(project)
        return project.provider {
            options.flatMap {  option ->
                val values = projection.project(option).get()
                values.map {
                    SubpluginOption(option.name, option.type.serializer.serialize(it))
                }
            }
        }
    }

    override fun isApplicable(kotlinCompilation: KotlinCompilation<*>): Boolean {
        val project = kotlinCompilation.target.project
        val sourceSet = runCatching { kotlinCompilation.defaultSourceSet }
            .getOrNull()
            ?: return run {
                val unsupportedCompilationWarning = project
                    .findProperty("dev.mokkery.unsupportedCompilationWarnings")
                    .toString()
                    .toBooleanStrictOrNull()
                    ?: true
                if (unsupportedCompilationWarning) {
                    val log = "w: Compilation {} might not support compiler plugins (e.g. known issue with Android test fixtures compilations)!" +
                            " Mokkery might not work correctly in associated source set!" +
                            " To hide this message, add 'dev.mokkery.unsupportedCompilationWarnings=false' to the Gradle properties."
                    project.logger.warn(log, kotlinCompilation.name)
                }
                false
            }
        return rule(project).isApplicable(sourceSet)
    }
}
