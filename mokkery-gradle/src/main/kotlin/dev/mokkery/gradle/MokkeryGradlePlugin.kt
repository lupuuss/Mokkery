@file:Suppress("DEPRECATION_ERROR")

package dev.mokkery.gradle

import dev.mokkery.internal.MokkeryConfig
import dev.mokkery.internal.MokkeryConfig.RUNTIME_DEPENDENCY
import dev.mokkery.internal.MokkeryConfig.VERSION
import dev.mokkery.internal.options.MokkeryOptionProjection
import dev.mokkery.internal.options.MokkeryOptions
import dev.mokkery.internal.options.MokkeryOptionsContainer
import org.gradle.api.Project
import org.jetbrains.kotlin.gradle.plugin.SubpluginArtifact
import org.jetbrains.kotlin.gradle.plugin.kotlinToolingVersion
import org.jetbrains.kotlin.tooling.core.KotlinToolingVersion
import org.jetbrains.kotlin.tooling.core.toKotlinVersion

/**
 * Configures Mokkery in source sets specified by [MokkeryGradleExtension.rule]. It includes:
 * * Adding runtime dependency
 * * Adding configured compiler plugin
 */
public class MokkeryGradlePlugin : BaseMokkeryGradlePlugin() {

    override val options: MokkeryOptionsContainer = MokkeryOptions

    override fun rule(project: Project): ApplicationRule = project.mokkery.rule.get()

    override fun optionProjection(project: Project): MokkeryOptionProjection<MokkeryGradleProperty<Any>> {
        val extension = project.mokkery
        return MokkeryOptionProjection { option ->
            val property = when (option) {
                MokkeryOptions.Core.defaultMockMode -> extension.defaultMockMode
                MokkeryOptions.Core.defaultVerifyMode -> extension.defaultVerifyMode
                MokkeryOptions.Core.ignoreFinalMembers -> extension.ignoreFinalMembers
                MokkeryOptions.Core.ignoreInlineMembers -> extension.ignoreInlineMembers
                MokkeryOptions.Core.enableFirDiagnostics -> extension.enableFirDiagnostics
                MokkeryOptions.Stubs.allowClassInheritance -> extension.stubs.allowClassInheritance
                MokkeryOptions.Stubs.allowConcreteClassInstantiation -> extension.stubs.allowConcreteClassInstantiation
                MokkeryOptions.Annotations.copyToMock -> extension.annotations.copyToMock
                else -> error("Missing mapping for option $option")
            }
            MokkeryGradleProperty.fromAsAny(property)
        }
    }

    override fun apply(target: Project) {
        target.checkKotlinSetup()
        val mokkery = target
            .extensions
            .create("mokkery", MokkeryGradleExtension::class.java)
        mokkery.rule.convention(ApplicationRule.AllTests)
        applyOptionConventions(target)
        target.afterEvaluate { project ->
            sourceSetsForDependencies(project).forEach {
                it.dependencies {
                    implementation(RUNTIME_DEPENDENCY)
                }
            }
        }
        super.apply(target)
    }

    override fun getCompilerPluginId(): String = MokkeryConfig.PLUGIN_ID

    override fun getPluginArtifact(): SubpluginArtifact {
        return SubpluginArtifact(
            groupId = MokkeryConfig.GROUP,
            artifactId = MokkeryConfig.PLUGIN_ARTIFACT_ID,
            version = VERSION,
        )
    }

    private fun Project.checkKotlinSetup() {
        if (extensions.findByName("kotlin") == null) {
            error("Kotlin plugin not applied! Mokkery requires kotlin plugin!")
        }
        val currentKotlinVersion = kotlinToolingVersion.toKotlinVersion()
        val minimumKotlinVersion = KotlinVersion(MokkeryConfig.MINIMUM_KOTLIN_VERSION)
        if (currentKotlinVersion < minimumKotlinVersion) {
            error("Current Kotlin version must be at least ${MokkeryConfig.MINIMUM_KOTLIN_VERSION}, but is $currentKotlinVersion! Downgrade Mokkery version or upgrade Kotlin version! Check https://mokkery.dev/docs/Setup#compatibility for compatibility guidelines!")
        }
        val compiledKotlinVersion = KotlinVersion(MokkeryConfig.COMPILED_KOTLIN_VERSION)
        val versionWarnings = project.findProperty("dev.mokkery.versionWarnings")
            .toString()
            .toBooleanStrictOrNull()
            ?: true
        val isPotentiallyIncompatibleKotlinVersion = currentKotlinVersion.major > compiledKotlinVersion.major
                || currentKotlinVersion.minor > compiledKotlinVersion.minor
        if (versionWarnings && isPotentiallyIncompatibleKotlinVersion) {
            val log = "w: Mokkery was compiled against Kotlin {}, but the current version is {}!" +
                    " It might cause compatibility issues, such as NoSuchMethodError, NoClassDefFoundError, etc." +
                    " Please report any issues at https://github.com/lupuuss/Mokkery/issues!" +
                    " To hide this message, add 'dev.mokkery.versionWarnings=false' to the Gradle properties."
            logger.warn(log, compiledKotlinVersion, currentKotlinVersion)
        }
    }


    private fun KotlinVersion(string: String): KotlinVersion = KotlinToolingVersion(string).toKotlinVersion()
}

private val Project.mokkery get() = extensions.getByType(MokkeryGradleExtension::class.java)
