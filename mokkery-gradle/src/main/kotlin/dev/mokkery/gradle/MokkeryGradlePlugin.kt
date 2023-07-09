package dev.mokkery.gradle

import dev.mokkery.MokkeryCompilerDefaults
import dev.mokkery.MokkeryConfig
import dev.mokkery.MokkeryConfig.RUNTIME_DEPENDENCY
import dev.mokkery.MokkeryConfig.SUPPORTED_KOTLIN_VERSIONS
import dev.mokkery.MokkeryConfig.VERSION
import dev.mokkery.verify.VerifyModeSerializer
import org.gradle.api.Project
import org.gradle.api.provider.Provider
import org.jetbrains.kotlin.gradle.dsl.kotlinExtension
import org.jetbrains.kotlin.gradle.plugin.KotlinCompilation
import org.jetbrains.kotlin.gradle.plugin.KotlinCompilerPluginSupportPlugin
import org.jetbrains.kotlin.gradle.plugin.KotlinPlatformType.androidJvm
import org.jetbrains.kotlin.gradle.plugin.KotlinPlatformType.jvm
import org.jetbrains.kotlin.gradle.plugin.SubpluginArtifact
import org.jetbrains.kotlin.gradle.plugin.SubpluginOption
import org.jetbrains.kotlin.gradle.plugin.kotlinToolingVersion

@Suppress("OPT_IN_USAGE")
class MokkeryGradlePlugin : KotlinCompilerPluginSupportPlugin {

    lateinit var kotlinVersion: String

    override fun apply(target: Project) {
        target.checkKotlinSetup()
        val mokkery = target.extensions.create("mokkery", MokkeryGradleExtension::class.java)
        mokkery.defaultMockMode.convention(MokkeryCompilerDefaults.mockMode)
        mokkery.defaultVerifyMode.convention(MokkeryCompilerDefaults.verifyMode)
        mokkery.rule.convention(ApplicationRule.AllTests)
        target.configureDependencies()
        super.apply(target)
    }

    override fun applyToCompilation(
        kotlinCompilation: KotlinCompilation<*>
    ): Provider<List<SubpluginOption>> = kotlinCompilation.run {
        kotlinCompilation.disableJvmParamAssertions()
        target.project.provider {
            listOf(
                SubpluginOption(key = "mockMode", value = project.mokkery.defaultMockMode.get().toString()),
                SubpluginOption(
                    key = "verifyMode",
                    value = VerifyModeSerializer.serialize(project.mokkery.defaultVerifyMode.get())
                )
            )
        }
    }

    override fun getCompilerPluginId(): String = MokkeryConfig.PLUGIN_ID

    override fun getPluginArtifact(): SubpluginArtifact {
        return SubpluginArtifact(
            groupId = MokkeryConfig.GROUP,
            artifactId = MokkeryConfig.PLUGIN_ARTIFACT_ID,
            version = pluginVersion,
        )
    }

    override fun isApplicable(kotlinCompilation: KotlinCompilation<*>): Boolean = kotlinCompilation
        .project
        .mokkery
        .rule
        .get()
        .isApplicable(kotlinCompilation.defaultSourceSet)

    private val pluginVersion get() = "$kotlinVersion-$VERSION"

    private fun Project.checkKotlinSetup() {
        if (extensions.findByName("kotlin") == null) error("Kotlin plugin not applied! Mokkery requires kotlin plugin!")
        val kotlinVersion = kotlinToolingVersion.toString()
        if (kotlinVersion !in SUPPORTED_KOTLIN_VERSIONS) {
            error(
                "Current kotlin version ($kotlinVersion) is unsupported by current Mokkery version!" +
                        " Mokkery version: $VERSION" +
                        " Supported kotlin versions: $SUPPORTED_KOTLIN_VERSIONS"
            )
        }
        this@MokkeryGradlePlugin.kotlinVersion = kotlinVersion
        mokkeryInfo("Selected plugin artifact based on kotlin version ($kotlinVersion) => $pluginVersion")
    }

    private fun KotlinCompilation<*>.disableJvmParamAssertions() {
        if (platformType in listOf(jvm, androidJvm)) {
            project.mokkeryInfo("-Xno-param-assertions flag applied for source set => ${defaultSourceSet.name}!")
            compilerOptions.options.freeCompilerArgs.add("-Xno-param-assertions")
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
            kotlinExtension
                .sourceSets
                .filter { rule.isApplicable(it) }
                .forEach {
                    mokkeryInfo("Runtime dependency $RUNTIME_DEPENDENCY applied to sourceSet: ${it.name}! ")
                    it.dependencies {
                        implementation(RUNTIME_DEPENDENCY)
                    }
                }
        }
    }

    private val Project.mokkery get() = extensions.getByType(MokkeryGradleExtension::class.java)
}
