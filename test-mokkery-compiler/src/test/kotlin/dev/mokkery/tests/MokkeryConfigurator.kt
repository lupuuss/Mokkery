package dev.mokkery.tests

import dev.mokkery.plugin.ENABLE_FIR_DIAGNOSTICS
import dev.mokkery.plugin.IGNORE_FINAL_MEMBERS
import dev.mokkery.plugin.IGNORE_INLINE_MEMBERS
import dev.mokkery.plugin.MokkeryFirRegistrar
import dev.mokkery.plugin.STUBS_ALLOW_CLASS_INHERITANCE
import dev.mokkery.plugin.STUBS_ALLOW_CONCRETE_CLASS_INSTANTIATION
import org.jetbrains.kotlin.cli.jvm.config.addJvmClasspathRoot
import org.jetbrains.kotlin.compiler.plugin.CompilerPluginRegistrar
import org.jetbrains.kotlin.compiler.plugin.ExperimentalCompilerApi
import org.jetbrains.kotlin.config.CompilerConfiguration
import org.jetbrains.kotlin.fir.extensions.FirExtensionRegistrarAdapter
import org.jetbrains.kotlin.test.model.TestModule
import org.jetbrains.kotlin.test.services.EnvironmentConfigurator
import org.jetbrains.kotlin.test.services.RuntimeClasspathProvider
import org.jetbrains.kotlin.test.services.TestServices
import java.io.File
import java.io.File.pathSeparator

private val mokkeryRuntimeClasspath = System.getProperty("mokkeryRuntime.classpath")
    ?.split(pathSeparator)
    ?.map(::File)
    ?: error("Unable to get a classpath from 'mokkeryRuntime.classpath'!")

class MokkeryConfigurator(testServices: TestServices) : EnvironmentConfigurator(testServices) {

    override fun configureCompilerConfiguration(configuration: CompilerConfiguration, module: TestModule) {
        module.directives.forEach {
            with(configuration) {
                when (it) {
                    MokkeryDirectives.IGNORE_FINAL_MEMBERS -> put(IGNORE_FINAL_MEMBERS, listOf(true))
                    MokkeryDirectives.IGNORE_INLINE_MEMBERS -> put(IGNORE_INLINE_MEMBERS, listOf(true))
                    MokkeryDirectives.DISABLE_FIR_DIAGNOSTICS -> put(ENABLE_FIR_DIAGNOSTICS, listOf(false))
                    MokkeryDirectives.STUBS_ALLOW_CLASS_INHERITANCE -> put(STUBS_ALLOW_CLASS_INHERITANCE, listOf(true))
                    MokkeryDirectives.STUBS_ALLOW_CONCRETE_CLASS_INSTANTIATION -> put(STUBS_ALLOW_CONCRETE_CLASS_INSTANTIATION, listOf(true))
                    else -> Unit
                }
            }
        }
        for (file in mokkeryRuntimeClasspath) {
            configuration.addJvmClasspathRoot(file)
        }
    }

    @OptIn(ExperimentalCompilerApi::class)
    override fun CompilerPluginRegistrar.ExtensionStorage.registerCompilerExtensions(
        module: TestModule,
        configuration: CompilerConfiguration,
    ) {
        module.languageVersionSettings
        FirExtensionRegistrarAdapter.registerExtension(MokkeryFirRegistrar(configuration))
    }

    class PathProvider(testServices: TestServices) : RuntimeClasspathProvider(testServices) {
        override fun runtimeClassPaths(module: TestModule) = mokkeryRuntimeClasspath
    }
}
