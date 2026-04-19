package dev.mokkery.mockable.plugin

import com.google.auto.service.AutoService
import dev.mokkery.mockable.internal.MokkeryMockableConfig
import dev.mokkery.mockable.internal.options.MokkeryMockableOptions
import dev.mokkery.plugin.core.BaseMokkeryCommandLineProcessor
import org.jetbrains.kotlin.compiler.plugin.CommandLineProcessor

@AutoService(CommandLineProcessor::class)
class MokkeryMockableCommandLineProcessor : BaseMokkeryCommandLineProcessor(
    pluginId = MokkeryMockableConfig.PLUGIN_ID,
    mokkeryOptions = MokkeryMockableOptions
)
