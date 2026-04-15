package dev.mokkery.plugin.core.context

import dev.mokkery.context.MokkeryContext
import dev.mokkery.plugin.core.MokkeryPluginScope
import org.jetbrains.kotlin.config.CompilerConfiguration

context(scope: MokkeryPluginScope)
val configuration: CompilerConfiguration
    get() = scope.readValue(configurationKey)

fun CompilerConfiguration.asMokkeryContext(): MokkeryContext = this.asMokkeryContext(configurationKey)

private val configurationKey = createValueKey<CompilerConfiguration>()
