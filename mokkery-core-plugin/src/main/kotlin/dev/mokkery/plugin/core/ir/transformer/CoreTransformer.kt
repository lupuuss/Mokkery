package dev.mokkery.plugin.core.ir.transformer

import dev.mokkery.context.MokkeryContext
import dev.mokkery.plugin.core.context.asMokkeryContext
import dev.mokkery.plugin.core.context.createValueKey
import dev.mokkery.plugin.core.context.readValue
import dev.mokkery.plugin.core.ir.IrMokkeryPluginScope
import org.jetbrains.kotlin.backend.common.IrElementTransformerVoidWithContext
import org.jetbrains.kotlin.backend.common.ScopeWithIr
import org.jetbrains.kotlin.ir.declarations.IrDeclaration
import org.jetbrains.kotlin.ir.declarations.IrFile

abstract class CoreTransformer(
    pluginScope: IrMokkeryPluginScope,
) : TransformerScope, IrElementTransformerVoidWithContext() {

    override val mokkeryContext = pluginScope.mokkeryContext + this.asMokkeryContext()

    val currentFileValue: IrFile
        get() = currentFile

    val currentScopeValue: ScopeWithIr
        get() = currentScope!!

    fun <T> withScope(declaration: IrDeclaration, block: () -> T): T {
        return withinScope(declaration.symbol.owner) {
            block()
        }
    }
}

context(scope: TransformerScope)
val transformer: CoreTransformer
    get() = scope.readValue(transformerKey)

private fun CoreTransformer.asMokkeryContext(): MokkeryContext {
    return asMokkeryContext(transformerKey)
}

private val transformerKey = createValueKey<CoreTransformer>()
