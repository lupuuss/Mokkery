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
import org.jetbrains.kotlin.ir.declarations.IrClass
import org.jetbrains.kotlin.ir.declarations.IrFile
import org.jetbrains.kotlin.ir.declarations.name
import org.jetbrains.kotlin.ir.expressions.IrCall
import org.jetbrains.kotlin.ir.expressions.IrExpression
import org.jetbrains.kotlin.ir.expressions.IrVarargElement
import org.jetbrains.kotlin.ir.symbols.IrSymbol
import org.jetbrains.kotlin.ir.types.IrType
import org.jetbrains.kotlin.ir.types.typeWith
import org.jetbrains.kotlin.ir.util.findDeclaration
import org.jetbrains.kotlin.ir.util.kotlinFqName
import org.jetbrains.kotlin.name.Name

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

context(scope: TransformerScope)
inline fun findOrBuildClassInCurrentFile(
    nameBase: String,
    nameHashSource: List<IrClass> = emptyList(),
    builder: (Name) -> IrClass
): IrClass {
    val name = generatedClassNameInCurrentFile(nameBase, nameHashSource)
    return currentFileValue.findDeclaration<IrClass> { it.name == name } ?: builder(name)
}

context(scope: TransformerScope)
fun generatedClassNameInCurrentFile(base: String, implements: List<IrClass>): Name {
    val prefix = when {
        implements.size == 1 -> "${implements[0].name}_${base}"
        else -> base
    }
    val hash = implements
        .map { it.kotlinFqName.asString() }
        .plus(currentFileValue.kotlinFqNameStringWithName())
        .hexHashString()
    return Name.identifier("${prefix}_${hash}")
}

private fun List<String>.hexHashString(): String {
    var hash = 0xcbf29ce484222325UL
    forEach {
        it.forEach { c -> hash = (hash xor c.code.toULong()) * 0x100000001b3UL }
    }
    return hash.toString(36)
}

private fun IrFile.kotlinFqNameStringWithName() = kotlinFqName.asString() + "." + this.name
