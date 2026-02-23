package dev.mokkery.plugin.ir.transformer.core

import dev.mokkery.plugin.ir.KotlinIr
import dev.mokkery.plugin.ir.MokkeryIr
import dev.mokkery.plugin.ir.irCall
import dev.mokkery.plugin.ir.irVararg
import dev.mokkery.plugin.ir.pluginContext
import dev.mokkery.plugin.ir.requirePropertyGetterOwner
import org.jetbrains.kotlin.backend.common.lower.DeclarationIrBuilder
import org.jetbrains.kotlin.ir.builders.IrBuilder
import org.jetbrains.kotlin.ir.builders.IrBuilderWithScope
import org.jetbrains.kotlin.ir.builders.IrGeneratorContext
import org.jetbrains.kotlin.ir.builders.irGetObject
import org.jetbrains.kotlin.ir.expressions.IrCall
import org.jetbrains.kotlin.ir.expressions.IrExpression
import org.jetbrains.kotlin.ir.expressions.IrVarargElement
import org.jetbrains.kotlin.ir.symbols.IrSymbol
import org.jetbrains.kotlin.ir.types.IrType
import org.jetbrains.kotlin.ir.types.typeWith

context(scope: TransformerScope)
inline fun <T> declarationIrBuilder(
    context: IrGeneratorContext = pluginContext,
    block: DeclarationIrBuilder.() -> T
) = DeclarationIrBuilder(context, currentScopeValue.scope.scopeOwnerSymbol).run(block)

context(scope: TransformerScope)
inline fun <T> IrSymbol.declarationIrBuilder(
    context: IrGeneratorContext = pluginContext,
    block: DeclarationIrBuilder.() -> T
) = DeclarationIrBuilder(context, this).run(block)

context(scope: TransformerScope)
inline val  IrSymbol.declarationIrBuilder: DeclarationIrBuilder
    get() = declarationIrBuilder { this }


context(scope: TransformerScope)
fun IrBuilder.irCallMapOf(
    pairs: List<Pair<IrExpression, IrExpression>>,
    keyType: IrType,
    valueType: IrType
) = irCall(referenced(KotlinIr.Function.mapOf)) {
    val varargs = irVararg(
        elementType = referenced(KotlinIr.Class.Pair).typeWith(keyType, valueType),
        elements = pairs.map { irCreatePair(it.first, it.second) }
    )
    typeArguments[0] = keyType
    typeArguments[1] = valueType
    arguments[0] = varargs
}


context(scope: TransformerScope)
fun IrBuilder.irCallListOf(
    type: IrType,
    elements: List<IrVarargElement>
) = irCall(referenced(KotlinIr.Function.listOf)) {
    arguments[0] = irVararg(elementType = type, elements = elements)
    typeArguments[0] = type
}

context(scope: TransformerScope)
fun IrBuilder.irCreatePair(
    first: IrExpression,
    second: IrExpression
): IrExpression = irCall(referenced(KotlinIr.Function.to)) {
    typeArguments[0] = first.type
    typeArguments[1] = second.type
    arguments[0] = first
    arguments[1] = second
}

context(scope: TransformerScope)
fun IrBuilderWithScope.irGetMokkeryScopeGlobal(): IrCall {
    val scopeCompanion = referencedCompanion(MokkeryIr.Class.MokkeryScope)
    return scopeCompanion
        .requirePropertyGetterOwner("global")
        .let { irCall(it) { arguments[0] = irGetObject(scopeCompanion.symbol) } }
}
