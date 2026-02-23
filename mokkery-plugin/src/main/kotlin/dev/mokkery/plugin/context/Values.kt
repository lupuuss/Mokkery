package dev.mokkery.plugin.context

import dev.mokkery.context.MokkeryContext
import dev.mokkery.plugin.MokkeryPluginScope
import org.jetbrains.kotlin.config.CompilerConfiguration

context(scope: MokkeryPluginScope)
val configuration: CompilerConfiguration
    get() = scope.readValue(configurationKey)

fun CompilerConfiguration.asMokkeryContext(): MokkeryContext = this.asMokkeryContext(configurationKey)

private val configurationKey = createValueKey<CompilerConfiguration>()
