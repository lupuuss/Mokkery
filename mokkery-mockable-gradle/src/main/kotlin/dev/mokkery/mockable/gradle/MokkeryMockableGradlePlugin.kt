package dev.mokkery.mockable.gradle

import dev.mokkery.gradle.ApplicationRule
import dev.mokkery.mockable.internal.MokkeryMockableConfig
import org.gradle.api.Project
import org.gradle.api.plugins.ExtensionAware

/**
 * Configures Mokkery Mockable in source sets specified by [MokkeryMockableGradleExtension.rule].
 */
public class MokkeryMockableGradlePlugin : BaseMokkeryGradlePluginWrapper() {

    override fun apply(target: Project) {
        val mokkeryExtension = target.extensions
            .findByName("mokkery")
            .let { it as? ExtensionAware }
            ?: error("dev.mokkery.mockable plugin requires dev.mokkery plugin with the same version, but dev.mokkery plugin was not found!")
        val mokkeryVersion = target.buildscript.configurations
            .findByName("classpath")
            ?.resolvedConfiguration
            ?.resolvedArtifacts
            ?.find { it.moduleVersion.id.module.group == "dev.mokkery" && it.moduleVersion.id.module.name == "mokkery-gradle" }
            ?.moduleVersion?.id?.version
        if (mokkeryVersion != MokkeryMockableConfig.VERSION) error(
            "dev.mokkery.mockable plugin requires dev.mokkery with exact version!" +
                    " Versions:" +
                    " dev.mokkery=${mokkeryVersion}" +
                    " dev.mokkery.mockable=${MokkeryMockableConfig.VERSION}"
        )
        val mockable = mokkeryExtension
            .extensions
            .create("mockable", MokkeryMockableGradleExtension::class.java)
        mockable.rule.convention(ApplicationRule.All)
        applyOptionConventions(target)
        target.afterEvaluate { project ->
            val mockableExtension = project.mokkeryExt.mockableExt
            val annotations = mockableExtension.annotations.get().toSet()
            if (mockableExtension.defaultAnnotations.any { it in annotations }) {
                sourceSetsForDependencies(project)
                    .forEach {
                        it.dependencies {
                            implementation(MokkeryMockableConfig.ANNOTATIONS_DEPENDENCY)
                        }
                    }
            }
        }
        super.apply(target)
    }
}

