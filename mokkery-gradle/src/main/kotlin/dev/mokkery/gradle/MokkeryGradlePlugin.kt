package dev.mokkery.gradle

import dev.mokkery.BuildConfig
import dev.mokkery.BuildConfig.MOKKERY_GROUP
import dev.mokkery.BuildConfig.MOKKERY_PLUGIN_ARTIFACT_ID
import dev.mokkery.BuildConfig.MOKKERY_RUNTIME
import dev.mokkery.BuildConfig.MOKKERY_SUPPORTED_KOTLIN_VERSIONS
import dev.mokkery.BuildConfig.MOKKERY_VERSION
import dev.mokkery.verify.VerifyModeSerializer
import org.gradle.api.Project
import org.gradle.api.provider.Provider
import org.jetbrains.kotlin.gradle.dsl.kotlinExtension
import org.jetbrains.kotlin.gradle.plugin.KotlinCompilation
import org.jetbrains.kotlin.gradle.plugin.KotlinCompilerPluginSupportPlugin
import org.jetbrains.kotlin.gradle.plugin.KotlinPlatformType.androidJvm
import org.jetbrains.kotlin.gradle.plugin.KotlinPlatformType.jvm
import org.jetbrains.kotlin.gradle.plugin.KotlinSourceSet
import org.jetbrains.kotlin.gradle.plugin.SubpluginArtifact
import org.jetbrains.kotlin.gradle.plugin.SubpluginOption
import org.jetbrains.kotlin.gradle.plugin.getKotlinPluginVersion

class MokkeryGradlePlugin : KotlinCompilerPluginSupportPlugin {

    lateinit var kotlinVersion: String

    override fun apply(target: Project) {
        val kotlinVersion = target.getKotlinPluginVersion()
        val versions = MOKKERY_SUPPORTED_KOTLIN_VERSIONS.split(", ")
        if (kotlinVersion !in versions) {
            error(
                "Current kotlin version ($kotlinVersion) is unsupported by current Mokkery version!" +
                    " Mokkery version: $MOKKERY_VERSION" +
                    " Supported kotlin versions: $versions"
            )
        }
        this.kotlinVersion = kotlinVersion
        target.extensions.create("mokkery", MokkeryGradleExtension::class.java).apply {
            targetSourceSets = setOfNotNull(
                target.kotlinExtension.sourceSets.findByName("commonTest")
            )
        }
        target.afterEvaluate {
            // https://youtrack.jetbrains.com/issue/KT-53477/Native-Gradle-plugin-doesnt-add-compiler-plugin-transitive-dependencies-to-compiler-plugin-classpath
            target.configurations.matching {
                it.name.startsWith("kotlin") && it.name.contains("CompilerPluginClasspath")
            }.all {
                it.isTransitive = true
            }
            target.mokkery
                .run { targetSourceSets - excludeSourceSets }
                .forEach {
                    it.dependencies {
                        implementation("$MOKKERY_GROUP:$MOKKERY_RUNTIME:$MOKKERY_VERSION")
                    }
                }
        }
        super.apply(target)
    }

    override fun applyToCompilation(
        kotlinCompilation: KotlinCompilation<*>
    ): Provider<List<SubpluginOption>> = kotlinCompilation.run {
        if (kotlinCompilation.platformType in listOf(jvm, androidJvm)) {
            kotlinCompilation.compilerOptions.options.freeCompilerArgs.add("-Xno-param-assertions")
        }
        target.project.provider {
            listOf(
                SubpluginOption(key = "mockMode", value = project.mokkery.defaultMockMode.toString()),
                SubpluginOption(key = "verifyMode", value = VerifyModeSerializer.serialize(project.mokkery.defaultVerifyMode))
            )
        }
    }

    override fun getCompilerPluginId(): String = BuildConfig.MOKKERY_PLUGIN_ID

    override fun getPluginArtifact(): SubpluginArtifact {
        return SubpluginArtifact(
            groupId = MOKKERY_GROUP,
            artifactId = MOKKERY_PLUGIN_ARTIFACT_ID,
            version = "$kotlinVersion-$MOKKERY_VERSION",
        )
    }

    override fun isApplicable(kotlinCompilation: KotlinCompilation<*>): Boolean {
        val extension = kotlinCompilation.project.mokkery
        val allDependsOn by lazy { kotlinCompilation.defaultSourceSet.allDependsOn }
        if (kotlinCompilation.defaultSourceSet in extension.excludeSourceSets) return false
        if (kotlinCompilation.defaultSourceSet in extension.targetSourceSets) return true
        return allDependsOn.any { it in extension.targetSourceSets && it !in extension.excludeSourceSets }
    }

    private val KotlinSourceSet.allDependsOn get(): List<KotlinSourceSet> = dependsOn.flatMap { it.allDependsOn + it }
}

private val Project.mokkery get() = extensions.getByType(MokkeryGradleExtension::class.java)
