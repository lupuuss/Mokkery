package dev.mokkery.plugin

import com.google.auto.service.AutoService
import dev.mokkery.MokkeryConfig
import dev.mokkery.internal.options.MokkeryOptions
import org.jetbrains.kotlin.cli.common.messages.CompilerMessageSeverity
import org.jetbrains.kotlin.compiler.plugin.AbstractCliOption
import org.jetbrains.kotlin.compiler.plugin.CommandLineProcessor
import org.jetbrains.kotlin.config.CompilerConfiguration
import org.jetbrains.kotlin.config.messageCollector

@AutoService(CommandLineProcessor::class)
class MokkeryCommandLineProcessor : CommandLineProcessor {

    override val pluginId = MokkeryConfig.PLUGIN_ID

    override val pluginOptions: Collection<AbstractCliOption> = MokkeryOptions.map { it.cliOption }

    override fun processOption(
        option: AbstractCliOption,
        value: String,
        configuration: CompilerConfiguration
    ) {
        val option = MokkeryOptions[option.optionName] ?: error("Unknown Mokkery CLI option: ${option.optionName}")
        val key = option.configurationKey
        try {
            val deserialized = option.type
                .serializer
                .deserialize(value)
            configuration.add(key, deserialized)
        } catch (e: Throwable) {
            throw IllegalStateException(
                "Could not deserialize value for Mokkery CLI option ${option.name} = <$value> Expected: ${option.type.description}",
                e
            )
        }
    }
}
