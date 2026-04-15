package dev.mokkery.plugin.core.ir

import dev.mokkery.context.MokkeryContext
import dev.mokkery.plugin.core.MokkeryPluginScope
import dev.mokkery.plugin.core.context.asMokkeryContext
import dev.mokkery.plugin.core.context.configuration
import dev.mokkery.plugin.core.context.createValueKey
import dev.mokkery.plugin.core.context.readValue
import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.config.messageCollector
import org.jetbrains.kotlin.ir.IrBuiltIns
import org.jetbrains.kotlin.ir.declarations.IrFactory

interface IrMokkeryPluginScope : MokkeryPluginScope

fun IrMokkeryPluginScope(
    context: MokkeryContext
): IrMokkeryPluginScope = object : IrMokkeryPluginScope {
    override val mokkeryContext = context
}

context(scope: IrMokkeryPluginScope)
val pluginContext: IrPluginContext
    get() = scope.readValue(pluginContextKey)

fun IrPluginContext.asMokkeryContext(): MokkeryContext = this.asMokkeryContext(pluginContextKey)

private val pluginContextKey = createValueKey<IrPluginContext>()

context(scope: IrMokkeryPluginScope)
inline val messageCollector get() = configuration.messageCollector

context(scope: IrMokkeryPluginScope)
inline val platform get() = pluginContext.platform

context(scope: IrMokkeryPluginScope)
inline val irBuiltIns: IrBuiltIns
    get() = pluginContext.irBuiltIns

context(scope: IrMokkeryPluginScope)
inline val irFactory: IrFactory
    get() = pluginContext.irFactory
