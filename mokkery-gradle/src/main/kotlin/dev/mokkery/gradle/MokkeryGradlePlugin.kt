@file:Suppress("DEPRECATION_ERROR")

package dev.mokkery.gradle

import dev.mokkery.MokkeryCompilerDefaults
import dev.mokkery.MokkeryConfig
import dev.mokkery.MokkeryConfig.RUNTIME_DEPENDENCY
import dev.mokkery.MokkeryConfig.VERSION
import dev.mokkery.verify.VerifyModeSerializer
import org.gradle.api.Project
import org.gradle.api.provider.Provider
import org.jetbrains.kotlin.gradle.dsl.kotlinExtension
import org.jetbrains.kotlin.gradle.plugin.KotlinCompilation
import org.jetbrains.kotlin.gradle.plugin.KotlinCompilerPluginSupportPlugin
import org.jetbrains.kotlin.gradle.plugin.SubpluginArtifact
import org.jetbrains.kotlin.gradle.plugin.SubpluginOption
import org.jetbrains.kotlin.gradle.plugin.kotlinToolingVersion
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinMetadataTarget
import org.jetbrains.kotlin.tooling.core.KotlinToolingVersion
import org.jetbrains.kotlin.tooling.core.toKotlinVersion

/**
 * Configures Mokkery in source sets specified by [MokkeryGradleExtension.rule]. It includes:
 * * Adding runtime dependency
 * * Adding configured compiler plugin
 */
public class MokkeryGradlePlugin : KotlinCompilerPluginSupportPlugin {

    override fun apply(target: Project) {
        target.checkKotlinSetup()
        val mokkery = target.extensions.create("mokkery", MokkeryGradleExtension::class.java)
        mokkery.defaultMockMode.convention(MokkeryCompilerDefaults.mockMode)
        mokkery.defaultVerifyMode.convention(MokkeryCompilerDefaults.verifyMode)
        mokkery.rule.convention(ApplicationRule.AllTests)
        mokkery.ignoreInlineMembers.convention(false)
        mokkery.ignoreFinalMembers.convention(false)
        mokkery.enableFirDiagnostics.convention(true)
        target.configureDependencies()
        super.apply(target)
    }

    override fun applyToCompilation(
        kotlinCompilation: KotlinCompilation<*>
    ): Provider<List<SubpluginOption>> = kotlinCompilation.run {
        target.project.provider {
            listOf(
                SubpluginOption(key = "mockMode", value = project.mokkery.defaultMockMode.get().toString()),
                SubpluginOption(
                    key = "verifyMode",
                    value = VerifyModeSerializer.serialize(project.mokkery.defaultVerifyMode.get())
                ),
                SubpluginOption(
                    key = "ignoreFinalMembers",
                    value = project.mokkery.ignoreFinalMembers.get().toString()
                ),
                SubpluginOption(
                    key = "ignoreInlineMembers",
                    value = project.mokkery.ignoreInlineMembers.get().toString()
                ),
                SubpluginOption(
                    key = "enableFirDiagnostics",
                    value = project.mokkery.enableFirDiagnostics.get().toString()
                )
            )
        }
    }

    override fun getCompilerPluginId(): String = MokkeryConfig.PLUGIN_ID

    override fun getPluginArtifact(): SubpluginArtifact {
        return SubpluginArtifact(
            groupId = MokkeryConfig.GROUP,
            artifactId = MokkeryConfig.PLUGIN_ARTIFACT_ID,
            version = VERSION,
        )
    }

    override fun isApplicable(kotlinCompilation: KotlinCompilation<*>): Boolean {
        if (kotlinCompilation.target is KotlinMetadataTarget) return true
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
        return project.mokkery.rule.get().isApplicable(sourceSet)
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

    private fun Project.configureDependencies() {
        afterEvaluate {
            // https://youtrack.jetbrains.com/issue/KT-53477/Native-Gradle-plugin-doesnt-add-compiler-plugin-transitive-dependencies-to-compiler-plugin-classpath
            configurations.matching {
                it.name.startsWith("kotlin") && it.name.contains("CompilerPluginClasspath")
            }.all {
                it.isTransitive = true
            }
            val rule = mokkery.rule.get()
            val applicableSourceSets = kotlinExtension
                .sourceSets
                .filter { rule.isApplicable(it) }
            applicableSourceSets
                .filter { sourceSet -> sourceSet.dependsOn.none { it in applicableSourceSets } }
                .forEach {
                    mokkeryInfo("Runtime dependency $RUNTIME_DEPENDENCY applied to sourceSet: ${it.name}! ")
                    it.dependencies {
                        implementation(RUNTIME_DEPENDENCY)
                    }
                }
        }
    }

    private val Project.mokkery get() = extensions.getByType(MokkeryGradleExtension::class.java)

    private fun KotlinVersion(string: String): KotlinVersion = KotlinToolingVersion(string).toKotlinVersion()
}
