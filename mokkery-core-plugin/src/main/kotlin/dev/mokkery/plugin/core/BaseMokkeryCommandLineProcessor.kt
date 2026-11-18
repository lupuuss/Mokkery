package dev.mokkery.plugin.core

import dev.mokkery.internal.options.MokkeryOptionsContainer
import org.jetbrains.kotlin.compiler.plugin.AbstractCliOption
import org.jetbrains.kotlin.config.CompilerConfiguration

abstract class BaseMokkeryCommandLineProcessor(
    private val pluginId: String,
    private val mokkeryOptions: MokkeryOptionsContainer,
) {

    val pluginOptions: Collection<AbstractCliOption> = mokkeryOptions.map { it.cliOption }

    fun processOption(
        option: AbstractCliOption,
        value: String,
        configuration: CompilerConfiguration
    ) {
        val option = mokkeryOptions[option.optionName] ?: error("Unknown $pluginId CLI option: ${option.optionName}")
        val key = option.configurationKey
        try {
            val deserialized = option.type
                .serializer
                .deserialize(value)
            configuration.add(key, deserialized)
        } catch (e: Throwable) {
            throw IllegalStateException(
                "Could not deserialize value for $pluginId CLI option ${option.name} = <$value> Expected: ${option.type.description}",
                e
            )
        }
    }
}

