package dev.mokkery.gradle

import dev.mokkery.MokkeryConfig
import dev.mokkery.MokkeryConfig.RUNTIME_DEPENDENCY
import dev.mokkery.MokkeryConfig.SUPPORTED_KOTLIN_VERSIONS
import dev.mokkery.MokkeryConfig.VERSION
import dev.mokkery.verify.VerifyModeSerializer
import org.gradle.api.Project
import org.gradle.api.provider.Provider
import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.dsl.kotlinExtension
import org.jetbrains.kotlin.gradle.plugin.KotlinCompilation
import org.jetbrains.kotlin.gradle.plugin.KotlinCompilerPluginSupportPlugin
import org.jetbrains.kotlin.gradle.plugin.KotlinPlatformType.androidJvm
import org.jetbrains.kotlin.gradle.plugin.KotlinPlatformType.jvm
import org.jetbrains.kotlin.gradle.plugin.SubpluginArtifact
import org.jetbrains.kotlin.gradle.plugin.SubpluginOption
import org.jetbrains.kotlin.gradle.plugin.kotlinToolingVersion

class MokkeryGradlePlugin : KotlinCompilerPluginSupportPlugin {

    lateinit var kotlinVersion: String

    @OptIn(ExperimentalKotlinGradlePluginApi::class)
    override fun apply(target: Project) {
        if (target.extensions.findByName("kotlin") == null) error("Kotlin plugin not applied! Mokkery requires kotlin plugin!")
        val kotlinVersion = target.kotlinToolingVersion.toString()
        if (kotlinVersion !in SUPPORTED_KOTLIN_VERSIONS) {
            error(
                "Current kotlin version ($kotlinVersion) is unsupported by current Mokkery version!" +
                        " Mokkery version: $VERSION" +
                        " Supported kotlin versions: $SUPPORTED_KOTLIN_VERSIONS"
            )
        }
        this.kotlinVersion = kotlinVersion
        target.mokkeryInfo("Selected plugin artifact based on kotlin version ($kotlinVersion) => $pluginVersion")
        target.extensions.create("mokkery", MokkeryGradleExtension::class.java)
        target.afterEvaluate {
            // https://youtrack.jetbrains.com/issue/KT-53477/Native-Gradle-plugin-doesnt-add-compiler-plugin-transitive-dependencies-to-compiler-plugin-classpath
            target.configurations.matching {
                it.name.startsWith("kotlin") && it.name.contains("CompilerPluginClasspath")
            }.all {
                it.isTransitive = true
            }
            val rule = target.mokkery.rule
            target.kotlinExtension
                .sourceSets
                .filter { rule.isApplicable(it) }
                .forEach {
                    target.mokkeryInfo("Runtime dependency $RUNTIME_DEPENDENCY applied to sourceSet: ${it.name}! ")
                    it.dependencies {
                        implementation(MokkeryConfig.RUNTIME_DEPENDENCY)
                    }
                }
        }
        super.apply(target)
    }

    override fun applyToCompilation(
        kotlinCompilation: KotlinCompilation<*>
    ): Provider<List<SubpluginOption>> = kotlinCompilation.run {
        if (kotlinCompilation.platformType in listOf(jvm, androidJvm)) {
            kotlinCompilation.project.mokkeryInfo(
                "-Xno-param-assertions flag applied for source set => ${kotlinCompilation.defaultSourceSet.name}!"
            )
            kotlinCompilation.compilerOptions.options.freeCompilerArgs.add("-Xno-param-assertions")
        }
        target.project.provider {
            listOf(
                SubpluginOption(key = "mockMode", value = project.mokkery.defaultMockMode.toString()),
                SubpluginOption(
                    key = "verifyMode",
                    value = VerifyModeSerializer.serialize(project.mokkery.defaultVerifyMode)
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
        .isApplicable(kotlinCompilation.defaultSourceSet)

    private val pluginVersion get() = "$kotlinVersion-$VERSION"
}

private val Project.mokkery get() = extensions.getByType(MokkeryGradleExtension::class.java)
