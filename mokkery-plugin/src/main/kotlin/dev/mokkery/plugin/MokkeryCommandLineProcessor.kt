package dev.mokkery.plugin

import com.google.auto.service.AutoService
import dev.mokkery.internal.MokkeryConfig
import dev.mokkery.internal.options.MokkeryOptions
import dev.mokkery.plugin.core.BaseMokkeryCommandLineProcessor
import org.jetbrains.kotlin.compiler.plugin.CommandLineProcessor

@AutoService(CommandLineProcessor::class)
class MokkeryCommandLineProcessor : BaseMokkeryCommandLineProcessor(
    pluginId = MokkeryConfig.PLUGIN_ID,
    mokkeryOptions = MokkeryOptions,
)
