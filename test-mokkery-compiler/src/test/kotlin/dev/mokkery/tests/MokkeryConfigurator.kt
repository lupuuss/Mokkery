package dev.mokkery.tests


import dev.mokkery.annotations.InternalMokkeryApi
import dev.mokkery.internal.options.MokkeryOption
import dev.mokkery.internal.options.MokkeryOptions
import dev.mokkery.plugin.MokkeryFirRegistrar
import dev.mokkery.plugin.configurationKey
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
            MokkeryDirectives.writeDirective(it, configuration)
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
