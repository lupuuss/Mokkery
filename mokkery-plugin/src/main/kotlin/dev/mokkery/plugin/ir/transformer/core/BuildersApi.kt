package dev.mokkery.plugin.ir.transformer.core

import dev.mokkery.plugin.core.ir.irBuiltIns
import dev.mokkery.plugin.core.ir.transformer.TransformerScope
import dev.mokkery.plugin.core.ir.transformer.currentFileValue
import dev.mokkery.plugin.core.ir.transformer.referenced
import dev.mokkery.plugin.core.ir.transformer.referencedCompanion
import dev.mokkery.plugin.core.ir.transformer.referencedDefaultType
import dev.mokkery.plugin.core.ir.transformer.referencedPrimaryConstructor
import dev.mokkery.plugin.ir.KotlinIr
import dev.mokkery.plugin.ir.MokkeryIr
import dev.mokkery.plugin.ir.irCall
import dev.mokkery.plugin.ir.irCallConstructor
import dev.mokkery.plugin.ir.irVararg
import dev.mokkery.plugin.ir.requireSimpleFunctionOwner
import dev.mokkery.plugin.ir.transformer.module.moduleScopePropertyAccessor
import org.jetbrains.kotlin.ir.builders.IrBuilder
import org.jetbrains.kotlin.ir.builders.IrBuilderWithScope
import org.jetbrains.kotlin.ir.builders.irGetObject
import org.jetbrains.kotlin.ir.builders.irInt
import org.jetbrains.kotlin.ir.declarations.IrClass
import org.jetbrains.kotlin.ir.declarations.IrFile
import org.jetbrains.kotlin.ir.declarations.name
import org.jetbrains.kotlin.ir.expressions.IrCall
import org.jetbrains.kotlin.ir.expressions.IrConstructorCall
import org.jetbrains.kotlin.ir.expressions.IrExpression
import org.jetbrains.kotlin.ir.expressions.IrVarargElement
import org.jetbrains.kotlin.ir.types.IrType
import org.jetbrains.kotlin.ir.types.IrTypeSystemContextImpl
import org.jetbrains.kotlin.ir.types.typeWith
import org.jetbrains.kotlin.ir.util.findDeclaration
import org.jetbrains.kotlin.ir.util.isSubtypeOf
import org.jetbrains.kotlin.ir.util.kotlinFqName
import org.jetbrains.kotlin.name.Name

context(scope: TransformerScope)
fun IrBuilder.irCallListOfPairs(
    pairs: List<Pair<IrExpression, IrExpression>>,
    firstType: IrType,
    secondType: IrType
) = irCall(referenced(KotlinIr.Function.listOf)) {
    val pairType = referenced(KotlinIr.Class.Pair).typeWith(firstType, secondType)
    val varargs = irVararg(
        elementType = pairType,
        elements = pairs.map { irCreatePair(it.first, it.second) }
    )
    typeArguments[0] = pairType
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
fun IrBuilder.irCallListGet(
    list: IrExpression,
    index: Int
): IrCall = irCall(irBuiltIns.listClass.owner.requireSimpleFunctionOwner("get"), irBuiltIns.anyNType) {
    arguments[0] = list
    arguments[1] = irInt(index)
}

context(scope: TransformerScope)
fun IrBuilder.irCallEqMatcher(
    value: IrExpression,
    valueType: IrType
): IrConstructorCall = irCallConstructor(
    constructor = referencedPrimaryConstructor(MokkeryIr.Class.ArgMatcherEquals),
    typeArguments = listOf(valueType)
) {
    arguments[0] = value
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

context(scope: TransformerScope)
fun IrBuilderWithScope.irGetMokkeryScopeFor(call: IrCall): IrExpression {
    val typeSystem = IrTypeSystemContextImpl(irBuiltIns)
    val mokkeryScope = referencedDefaultType(MokkeryIr.Class.MokkeryScope)
    val scopeParam = call.symbol.owner.parameters.find { it.type.isSubtypeOf(mokkeryScope, typeSystem) }
    if (scopeParam == null) return irGetMokkeryModuleScope()
    return call.arguments[scopeParam]!!
}

context(scope: TransformerScope)
fun IrBuilderWithScope.irGetMokkeryModuleScope(): IrCall {
    val scopeCompanion = referencedCompanion(MokkeryIr.Class.MokkeryScope)
    return irCall(moduleScopePropertyAccessor) {
        arguments[0] = irGetObject(scopeCompanion.symbol)
    }
}

private fun List<String>.hexHashString(): String {
    var hash = 0xcbf29ce484222325UL
    forEach {
        it.forEach { c -> hash = (hash xor c.code.toULong()) * 0x100000001b3UL }
    }
    return hash.toString(36)
}

private fun IrFile.kotlinFqNameStringWithName() = kotlinFqName.asString() + "." + this.name
