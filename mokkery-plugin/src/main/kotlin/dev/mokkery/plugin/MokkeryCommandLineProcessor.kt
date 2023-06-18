package dev.mokkery.plugin

import com.google.auto.service.AutoService
import dev.mokkery.BuildConfig
import dev.mokkery.MockMode
import org.jetbrains.kotlin.compiler.plugin.AbstractCliOption
import org.jetbrains.kotlin.compiler.plugin.CliOption
import org.jetbrains.kotlin.compiler.plugin.CommandLineProcessor
import org.jetbrains.kotlin.config.CompilerConfiguration
import org.jetbrains.kotlin.config.CompilerConfigurationKey

val MOCK_MODE_KEY = CompilerConfigurationKey<List<MockMode>>("mockMode")

@AutoService(CommandLineProcessor::class)
class MokkeryCommandLineProcessor : CommandLineProcessor {
    override val pluginId = BuildConfig.MOKKERY_PLUGIN_ID

    override val pluginOptions: Collection<AbstractCliOption> = listOf(
        CliOption(
            optionName = MOCK_MODE_KEY.toString(),
            valueDescription = "enum class dev.mokkery.MockMode",
            description = "Default MockMode for every mock.",
            required = true,
            allowMultipleOccurrences = false
        )
    )

    override fun processOption(option: AbstractCliOption, value: String, configuration: CompilerConfiguration) {
        return when (option.optionName) {
            MOCK_MODE_KEY.toString() -> configuration.add(MOCK_MODE_KEY, MockMode.valueOf(value))
            else -> error("Unknown config option: $option")
        }
    }
}
