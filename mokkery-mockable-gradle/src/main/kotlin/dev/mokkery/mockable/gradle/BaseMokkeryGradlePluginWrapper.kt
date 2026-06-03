package dev.mokkery.mockable.gradle

import dev.mokkery.annotations.InternalMokkeryApi
import dev.mokkery.gradle.ApplicationRule
import dev.mokkery.gradle.BaseMokkeryGradlePlugin
import dev.mokkery.gradle.MokkeryGradleExtension
import dev.mokkery.gradle.MokkeryGradleProperty
import dev.mokkery.internal.options.MokkeryOptionProjection
import dev.mokkery.internal.options.MokkeryOptionsContainer
import dev.mokkery.mockable.internal.MokkeryMockableConfig
import dev.mokkery.mockable.internal.options.MokkeryMockableOptions
import org.gradle.api.Project
import org.gradle.api.plugins.ExtensionAware
import org.gradle.api.provider.Provider
import org.jetbrains.kotlin.gradle.plugin.KotlinCompilation
import org.jetbrains.kotlin.gradle.plugin.KotlinCompilerPluginSupportPlugin
import org.jetbrains.kotlin.gradle.plugin.KotlinSourceSet
import org.jetbrains.kotlin.gradle.plugin.SubpluginArtifact
import org.jetbrains.kotlin.gradle.plugin.SubpluginOption

/**
 * Wrapper for [BaseMokkeryGradlePlugin] that avoids [java.lang.NoClassDefFoundError] when `dev.mokkery` plugin is not present.
 */
@InternalMokkeryApi
public abstract class BaseMokkeryGradlePluginWrapper : KotlinCompilerPluginSupportPlugin {

    private val impl by lazy { impl() }

    public fun applyOptionConventions(project: Project): Unit = (impl as BaseMokkeryGradlePlugin)
        .applyOptionConventions(project)

    public fun sourceSetsForDependencies(project: Project): List<KotlinSourceSet> = (impl as BaseMokkeryGradlePlugin)
        .sourceSetsForDependencies(project)

    override fun applyToCompilation(
        kotlinCompilation: KotlinCompilation<*>
    ): Provider<List<SubpluginOption>> = impl.applyToCompilation(kotlinCompilation)

    override fun getCompilerPluginId(): String = impl.getCompilerPluginId()

    override fun getPluginArtifact(): SubpluginArtifact = impl.getPluginArtifact()

    override fun isApplicable(kotlinCompilation: KotlinCompilation<*>): Boolean = impl.isApplicable(kotlinCompilation)
}

private fun impl(): KotlinCompilerPluginSupportPlugin = object : BaseMokkeryGradlePlugin() {

    override fun getCompilerPluginId(): String = MokkeryMockableConfig.PLUGIN_ID

    override fun getPluginArtifact(): SubpluginArtifact = SubpluginArtifact(
        groupId = MokkeryMockableConfig.GROUP,
        artifactId = MokkeryMockableConfig.PLUGIN_ARTIFACT_ID,
        version = MokkeryMockableConfig.VERSION
    )

    override val options: MokkeryOptionsContainer = MokkeryMockableOptions

    override fun optionProjection(project: Project): MokkeryOptionProjection<MokkeryGradleProperty<Any>> {
        val mokkery = project.mokkeryExt
        val mockable = mokkery.mockableExt
        return MokkeryOptionProjection { option ->
            when (option) {
                MokkeryMockableOptions.annotations -> MokkeryGradleProperty.fromAsAny(mockable.annotations)
                MokkeryMockableOptions.enableFirDiagnostics -> MokkeryGradleProperty.fromAsAny(mokkery.enableFirDiagnostics)
                else -> error("Missing mapping for option $option")
            }
        }
    }

    override fun rule(project: Project): ApplicationRule = project.mokkeryExt.mockableExt.rule.get()
}

internal val Project.mokkeryExt: MokkeryGradleExtension
    get() = extensions.getByType(MokkeryGradleExtension::class.java)

internal val MokkeryGradleExtension.mockableExt: MokkeryMockableGradleExtension
    get() = (this as ExtensionAware).extensions.getByType(MokkeryMockableGradleExtension::class.java)
