package dev.mokkery.plugin.core.ir.transformer

import dev.mokkery.plugin.core.ir.pluginContext
import org.jetbrains.kotlin.backend.common.lower.DeclarationIrBuilder
import org.jetbrains.kotlin.ir.builders.IrGeneratorContext
import org.jetbrains.kotlin.ir.expressions.IrExpression
import org.jetbrains.kotlin.ir.symbols.IrSymbol

context(scope: TransformerScope)
inline fun <T> IrSymbol.declarationIrBuilder(
    context: IrGeneratorContext = pluginContext,
    block: DeclarationIrBuilder.() -> T
) = DeclarationIrBuilder(context, this).run(block)

context(scope: TransformerScope)
inline fun <T> IrExpression.replaceDeclarationIrBuilder(
    context: IrGeneratorContext = pluginContext,
    block: DeclarationIrBuilder.() -> T
) = DeclarationIrBuilder(
    generatorContext = context,
    symbol = currentScopeValue.scope.scopeOwnerSymbol,
    startOffset = this.startOffset,
    endOffset = this.endOffset
).run(block)

context(scope: TransformerScope)
inline val IrSymbol.declarationIrBuilder: DeclarationIrBuilder
    get() = declarationIrBuilder { this }
