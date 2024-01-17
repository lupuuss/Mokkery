package dev.mokkery.plugin

import com.google.auto.service.AutoService
import dev.mokkery.MockMode
import dev.mokkery.MokkeryConfig
import dev.mokkery.verify.VerifyMode
import dev.mokkery.verify.VerifyModeSerializer
import org.jetbrains.kotlin.compiler.plugin.AbstractCliOption
import org.jetbrains.kotlin.compiler.plugin.CliOption
import org.jetbrains.kotlin.compiler.plugin.CommandLineProcessor
import org.jetbrains.kotlin.config.CompilerConfiguration
import org.jetbrains.kotlin.config.CompilerConfigurationKey

val MOCK_MODE_KEY = CompilerConfigurationKey<List<MockMode>>("mockMode")
val VERIFY_MODE_KEY = CompilerConfigurationKey<List<VerifyMode>>("verifyMode")
val ALLOW_INDIRECT_SUPER_CALLS = CompilerConfigurationKey<List<Boolean>>("allowIndirectSuperCalls")

@AutoService(CommandLineProcessor::class)
class MokkeryCommandLineProcessor : CommandLineProcessor {

    override val pluginId = MokkeryConfig.PLUGIN_ID

    override val pluginOptions: Collection<AbstractCliOption> = listOf(
        CliOption(
            optionName = MOCK_MODE_KEY.toString(),
            valueDescription = "enum class dev.mokkery.MockMode",
            description = "Default MockMode for every mock.",
            required = true,
            allowMultipleOccurrences = false
        ),
        CliOption(
            optionName = VERIFY_MODE_KEY.toString(),
            valueDescription = "sealed class dev.mokkery.VerifyMode",
            description = "Default VerifyMode for every verify block.",
            required = true,
            allowMultipleOccurrences = false
        ),
        CliOption(
            optionName = ALLOW_INDIRECT_SUPER_CALLS.toString(),
            valueDescription = "Boolean",
            description = "Dictates if super calls to indirect types should be allowed whenever possible.",
            required = true,
            allowMultipleOccurrences = false
        )
    )

    override fun processOption(option: AbstractCliOption, value: String, configuration: CompilerConfiguration) {
        return when (option.optionName) {
            MOCK_MODE_KEY.toString() -> configuration.add(MOCK_MODE_KEY, MockMode.valueOf(value))
            VERIFY_MODE_KEY.toString() -> configuration.add(VERIFY_MODE_KEY, VerifyModeSerializer.deserialize(value))
            ALLOW_INDIRECT_SUPER_CALLS.toString() -> configuration.add(ALLOW_INDIRECT_SUPER_CALLS, value.toBoolean())
            else -> error("Unknown config option: $option")
        }
    }
}
