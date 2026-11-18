package dev.mokkery.plugin

import dev.mokkery.internal.MokkeryConfig
import dev.mokkery.internal.options.MokkeryOptions
import dev.mokkery.plugin.core.BaseMokkeryCommandLineProcessor
import org.jetbrains.kotlin.compiler.plugin.devkit.DevKitCLP

class MokkeryCommandLineProcessor : BaseMokkeryCommandLineProcessor(
    pluginId = MokkeryConfig.PLUGIN_ID,
    mokkeryOptions = MokkeryOptions,
), DevKitCLP
