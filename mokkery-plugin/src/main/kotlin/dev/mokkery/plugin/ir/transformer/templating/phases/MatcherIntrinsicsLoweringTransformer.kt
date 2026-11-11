package dev.mokkery.plugin.ir.transformer.templating.phases

import dev.mokkery.plugin.core.ir.IrMokkeryPluginScope
import dev.mokkery.plugin.core.ir.irBuiltIns
import dev.mokkery.plugin.core.ir.transformer.CoreTransformer
import dev.mokkery.plugin.core.ir.transformer.referenced
import dev.mokkery.plugin.core.ir.transformer.replaceDeclarationIrBuilder
import dev.mokkery.plugin.ir.MokkeryIr
import dev.mokkery.plugin.ir.argumentTypes
import dev.mokkery.plugin.ir.irCall
import dev.mokkery.plugin.ir.irVararg
import dev.mokkery.plugin.ir.transformer.templating.matcher.MatcherFunctionKind
import dev.mokkery.plugin.ir.transformer.templating.matcher.matcherFunctionKind
import org.jetbrains.kotlin.ir.expressions.IrCall
import org.jetbrains.kotlin.ir.expressions.IrExpression
import org.jetbrains.kotlin.ir.expressions.IrVararg

/**
 * Replaces matcher intrinsics with real code: `matches` and `materialize` with their argument,
 * vararg `materialize` with `internalMaterialize`, and `spreadArray` with `internalSpreadArray`.
 *
 * Assumes [MatcherRetypingTransformer] ran, so the intrinsic calls already have matcher types.
 */
class MatcherIntrinsicsLoweringTransformer(pluginScope: IrMokkeryPluginScope) : CoreTransformer(pluginScope) {

    override fun visitCall(expression: IrCall): IrExpression {
        expression.transformChildrenVoid()
        return when (expression.symbol.owner.matcherFunctionKind()) {
            MatcherFunctionKind.Matches, MatcherFunctionKind.Materializer -> expression.arguments[1]!!
            MatcherFunctionKind.SpreadArray -> expression.replaceDeclarationIrBuilder {
                irCall(referenced(MokkeryIr.Function.internalSpreadArray), expression.type) {
                    typeArguments[0] = expression.type.argumentTypes.single()!!.argumentTypes.single() ?: irBuiltIns.anyNType
                    arguments[0] = expression.arguments[1]
                }
            }
            MatcherFunctionKind.MaterializerVararg -> expression.replaceDeclarationIrBuilder {
                val matcherType = expression.type.argumentTypes.single()!!
                irCall(referenced(MokkeryIr.Function.internalMaterialize), expression.type) {
                    typeArguments[0] = matcherType.argumentTypes.single() ?: irBuiltIns.anyNType
                    arguments[0] = irVararg(
                        elementType = matcherType,
                        elements = (expression.arguments[1] as? IrVararg)?.elements.orEmpty()
                    )
                }
            }
            MatcherFunctionKind.Builder, null -> expression
        }
    }
}
