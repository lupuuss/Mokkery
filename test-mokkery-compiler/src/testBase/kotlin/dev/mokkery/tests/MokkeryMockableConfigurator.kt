package dev.mokkery.mockable.tests.config

import dev.mokkery.mockable.internal.options.MokkeryMockableOptions
import dev.mokkery.mockable.plugin.MokkeryMockableFirRegistrar
import dev.mokkery.plugin.core.configurationKey
import dev.mokkery.tests.MokkeryMockableDirectives
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

private val mokkeryMockableRuntimeClasspath = System.getProperty("mokkery.mockable.runtimeClasspath")
    ?.split(pathSeparator)
    ?.map(::File)
    ?: error("Unable to get a classpath from 'mokkery.runtimeClasspath'!")

class MokkeryMockableConfigurator(testServices: TestServices) : EnvironmentConfigurator(testServices) {

    override fun configureCompilerConfiguration(configuration: CompilerConfiguration, module: TestModule) {
        module.directives.forEach {
            MokkeryMockableDirectives.writeDirective(it, configuration)
        }
        configuration.put(
            MokkeryMockableOptions.annotations.configurationKey,
            listOf("dev.mokkery.mockable.annotations.Mockable")
        )
        for (file in mokkeryMockableRuntimeClasspath) {
            configuration.addJvmClasspathRoot(file)
        }
    }

    @OptIn(ExperimentalCompilerApi::class)
    override fun CompilerPluginRegistrar.ExtensionStorage.registerCompilerExtensions(
        module: TestModule,
        configuration: CompilerConfiguration,
    ) {
        FirExtensionRegistrarAdapter.registerExtension(MokkeryMockableFirRegistrar(configuration))
    }

    class PathProvider(testServices: TestServices) : RuntimeClasspathProvider(testServices) {
        override fun runtimeClassPaths(module: TestModule) = mokkeryMockableRuntimeClasspath
    }
}
