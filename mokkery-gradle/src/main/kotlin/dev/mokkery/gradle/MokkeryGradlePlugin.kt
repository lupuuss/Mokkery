package dev.mokkery.gradle

import dev.mokkery.BuildConfig
import dev.mokkery.BuildConfig.MOKKERY_RUNTIME
import dev.mokkery.BuildConfig.MOKKERY_GROUP
import dev.mokkery.BuildConfig.MOKKERY_PLUGIN_ARTIFACT_ID
import dev.mokkery.BuildConfig.MOKKERY_VERSION
import org.jetbrains.kotlin.gradle.dsl.kotlinExtension
import org.jetbrains.kotlin.gradle.plugin.KotlinCompilation
import org.jetbrains.kotlin.gradle.plugin.KotlinCompilerPluginSupportPlugin
import org.jetbrains.kotlin.gradle.plugin.SubpluginArtifact
import org.jetbrains.kotlin.gradle.plugin.SubpluginOption

class MokkeryGradlePlugin : KotlinCompilerPluginSupportPlugin {

    override fun applyToCompilation(kotlinCompilation: KotlinCompilation<*>) = kotlinCompilation.run {
        val commonTest = kotlinCompilation.project.kotlinExtension.sourceSets.getByName("commonTest")
        val sourceSet = commonTest ?: defaultSourceSet
        sourceSet.dependencies {
            implementation("$MOKKERY_GROUP:$MOKKERY_RUNTIME:$MOKKERY_VERSION")
        }
        project.provider { emptyList<SubpluginOption>() }
    }

    override fun getCompilerPluginId(): String = BuildConfig.MOKKERY_PLUGIN_ID

    override fun getPluginArtifact(): SubpluginArtifact = SubpluginArtifact(
        groupId = MOKKERY_GROUP,
        artifactId = MOKKERY_PLUGIN_ARTIFACT_ID,
        version = MOKKERY_VERSION,
    )

    override fun isApplicable(kotlinCompilation: KotlinCompilation<*>): Boolean = kotlinCompilation
        .defaultSourceSet
        .name
        .contains("test", ignoreCase = true)
}
