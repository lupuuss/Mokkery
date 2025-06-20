package dev.mokkery.plugin.ir

import dev.mokkery.plugin.core.IrMokkeryKind
import dev.mokkery.plugin.core.Kotlin
import dev.mokkery.plugin.core.Mokkery
import dev.mokkery.plugin.core.TransformerScope
import dev.mokkery.plugin.core.getClass
import dev.mokkery.plugin.core.getFunction
import org.jetbrains.kotlin.backend.common.lower.DeclarationIrBuilder
import org.jetbrains.kotlin.backend.common.lower.irIfThen
import org.jetbrains.kotlin.backend.common.lower.irNot
import org.jetbrains.kotlin.descriptors.DescriptorVisibilities
import org.jetbrains.kotlin.ir.UNDEFINED_OFFSET
import org.jetbrains.kotlin.ir.builders.IrBlockBodyBuilder
import org.jetbrains.kotlin.ir.builders.IrBuilder
import org.jetbrains.kotlin.ir.builders.IrBuilderWithScope
import org.jetbrains.kotlin.ir.builders.declarations.addValueParameter
import org.jetbrains.kotlin.ir.builders.declarations.buildFun
import org.jetbrains.kotlin.ir.builders.irBlockBody
import org.jetbrains.kotlin.ir.builders.irCall
import org.jetbrains.kotlin.ir.builders.irCallConstructor
import org.jetbrains.kotlin.ir.builders.irDelegatingConstructorCall
import org.jetbrains.kotlin.ir.builders.irEqualsNull
import org.jetbrains.kotlin.ir.builders.irGet
import org.jetbrains.kotlin.ir.builders.irSetField
import org.jetbrains.kotlin.ir.declarations.IrClass
import org.jetbrains.kotlin.ir.declarations.IrConstructor
import org.jetbrains.kotlin.ir.declarations.IrDeclarationOrigin
import org.jetbrains.kotlin.ir.declarations.IrDeclarationParent
import org.jetbrains.kotlin.ir.declarations.IrProperty
import org.jetbrains.kotlin.ir.declarations.IrSimpleFunction
import org.jetbrains.kotlin.ir.declarations.IrValueParameter
import org.jetbrains.kotlin.ir.expressions.IrCall
import org.jetbrains.kotlin.ir.expressions.IrClassReference
import org.jetbrains.kotlin.ir.expressions.IrDelegatingConstructorCall
import org.jetbrains.kotlin.ir.expressions.IrExpression
import org.jetbrains.kotlin.ir.expressions.IrFunctionAccessExpression
import org.jetbrains.kotlin.ir.expressions.IrFunctionExpression
import org.jetbrains.kotlin.ir.expressions.IrGetEnumValue
import org.jetbrains.kotlin.ir.expressions.IrSetField
import org.jetbrains.kotlin.ir.expressions.IrSpreadElement
import org.jetbrains.kotlin.ir.expressions.IrStatementOrigin
import org.jetbrains.kotlin.ir.expressions.IrVararg
import org.jetbrains.kotlin.ir.expressions.IrVarargElement
import org.jetbrains.kotlin.ir.expressions.IrWhen
import org.jetbrains.kotlin.ir.expressions.impl.IrCallImpl
import org.jetbrains.kotlin.ir.expressions.impl.IrClassReferenceImpl
import org.jetbrains.kotlin.ir.expressions.impl.IrFunctionExpressionImpl
import org.jetbrains.kotlin.ir.expressions.impl.IrGetEnumValueImpl
import org.jetbrains.kotlin.ir.expressions.impl.IrSpreadElementImpl
import org.jetbrains.kotlin.ir.expressions.impl.IrVarargImpl
import org.jetbrains.kotlin.ir.symbols.IrClassSymbol
import org.jetbrains.kotlin.ir.symbols.IrSimpleFunctionSymbol
import org.jetbrains.kotlin.ir.types.IrSimpleType
import org.jetbrains.kotlin.ir.types.IrType
import org.jetbrains.kotlin.ir.types.classifierOrFail
import org.jetbrains.kotlin.ir.types.starProjectedType
import org.jetbrains.kotlin.ir.types.typeOrFail
import org.jetbrains.kotlin.ir.types.typeWith
import org.jetbrains.kotlin.ir.util.constructors
import org.jetbrains.kotlin.ir.util.defaultConstructor
import org.jetbrains.kotlin.ir.util.defaultType
import org.jetbrains.kotlin.ir.util.eraseTypeParameters
import org.jetbrains.kotlin.ir.util.invokeFun
import org.jetbrains.kotlin.ir.util.isSuspendFunction
import org.jetbrains.kotlin.ir.util.isVararg
import org.jetbrains.kotlin.ir.util.kotlinFqName
import org.jetbrains.kotlin.ir.util.primaryConstructor
import org.jetbrains.kotlin.name.Name

// use until resolved https://youtrack.jetbrains.com/issue/KT-66178/kClassReference-extension-returns-incorrect-IrClassReferenceImpl
fun IrBuilder.kClassReference(classType: IrType): IrClassReference = IrClassReferenceImpl(
    startOffset = startOffset,
    endOffset = endOffset,
    type = context.irBuiltIns.kClassClass.starProjectedType,
    symbol = classType.classifierOrFail,
    classType = classType
)

fun IrBuilder.irGetEnumEntry(
    irClass: IrClass,
    name: String
): IrGetEnumValue = IrGetEnumValueImpl(
    startOffset = startOffset,
    endOffset = endOffset,
    type = irClass.defaultType,
    symbol = irClass.getEnumEntry(name).symbol
)

fun IrBuilder.irCallConstructor(
    constructor: IrConstructor
) = irCallConstructor(callee = constructor.symbol, typeArguments = emptyList())

fun IrBlockBodyBuilder.irDelegatingDefaultConstructorOrAny(
    transformer: TransformerScope,
    irClass: IrClass?
): IrDelegatingConstructorCall {
    val defaultConstructor = irClass?.defaultConstructor
    return when {
        irClass == null -> irDelegatingConstructorCall(context.irBuiltIns.anyClass.owner.primaryConstructor!!)
        defaultConstructor != null -> irDelegatingConstructorCall(defaultConstructor)
        else -> {
            val autofillFun = transformer.getFunction(Mokkery.Function.autofillConstructor)
            val constructor = irClass.primaryConstructor
                ?: irClass.constructors.firstOrNull()
                ?: error("No constructor found for ${irClass.kotlinFqName.asString()}!")
            irDelegatingConstructorCall(constructor).apply {
                constructor.parameters.forEach {
                    arguments[it] = irCall(autofillFun) {
                        type = it.type
                        typeArguments[0] = it.type
                        arguments[0] = kClassReference(it.type.eraseTypeParameters())
                    }
                }
            }
        }
    }
}

fun IrBuilderWithScope.irIfNotNull(arg: IrExpression, then: IrExpression): IrWhen {
    return irIfThen(
        condition = irNot(irEqualsNull(argument = arg)),
        thenPart = then
    )
}

fun IrBuilder.irLambda(
    lambdaType: IrType,
    parent: IrDeclarationParent,
    block: IrBlockBodyBuilder.(IrSimpleFunction) -> Unit
): IrFunctionExpression {
    val typeParams = (lambdaType as IrSimpleType).arguments.map { it.typeOrFail }
    val returnType = typeParams.last()
    val params = typeParams.dropLast(1)
    val lambda = context.irFactory.buildFun {
        this.startOffset = UNDEFINED_OFFSET
        this.endOffset = UNDEFINED_OFFSET
        this.name = Name.identifier("invoke")
        this.returnType = returnType
        this.isSuspend = lambdaType.isSuspendFunction()
        visibility = DescriptorVisibilities.LOCAL
        origin = IrDeclarationOrigin.LOCAL_FUNCTION_FOR_LAMBDA
    }.apply {
        val bodyBuilder = DeclarationIrBuilder(context, symbol, startOffset, endOffset)
        params.forEachIndexed { i, it ->
            addValueParameter("p${i + 1}", it)
        }
        this.parent = parent
        body = bodyBuilder.irBlockBody {
            block(this@apply)
        }
    }
    return IrFunctionExpressionImpl(
        startOffset = UNDEFINED_OFFSET,
        endOffset = UNDEFINED_OFFSET,
        type = lambdaType,
        function = lambda,
        origin = IrStatementOrigin.LAMBDA
    )
}

fun IrBuilderWithScope.irInvokeIfNotNull(
    function: IrExpression,
    isSuspend: Boolean,
    vararg args: IrExpression
): IrWhen {
    return irIfNotNull(
        function,
        irInvoke(function, isSuspend, *args)
    )
}

fun IrBuilder.irInvoke(
    function: IrExpression,
    isSuspend: Boolean,
    vararg args: IrExpression
): IrFunctionAccessExpression {
    val functionClass =
        context.irBuiltIns.let { if (isSuspend) it.suspendFunctionN(args.size) else it.functionN(args.size) }
    return irCall(functionClass.invokeFun!!) {
        arguments[0] = function
        args.forEachIndexed { index, arg ->
            arguments[index + 1] = arg
        }
    }
}

inline fun IrBuilder.irCall(symbol: IrSimpleFunctionSymbol, block: IrCall.() -> Unit = { }): IrCall {
    return irCall(symbol, symbol.owner.returnType).apply(block)
}

inline fun IrBuilder.irCall(
    func: IrSimpleFunction,
    type: IrType = func.returnType,
    block: IrCall.() -> Unit = { }
): IrCall {
    return irCall(func.symbol, type).apply(block)
}

fun IrBuilder.irCall(
    symbol: IrSimpleFunctionSymbol,
    type: IrType = symbol.owner.returnType,
    typeArgumentsCount: Int = symbol.owner.typeParameters.size,
    origin: IrStatementOrigin? = null,
    superQualifierSymbol: IrClassSymbol? = null,
    block: IrCall.() -> Unit = { }
): IrCall = IrCallImpl(
    startOffset = startOffset,
    endOffset = endOffset,
    type = type,
    symbol = symbol,
    typeArgumentsCount = typeArgumentsCount,
    origin = origin,
    superQualifierSymbol = superQualifierSymbol
).apply(block)

inline fun IrBuilder.irCallConstructor(
    constructor: IrConstructor,
    block: IrFunctionAccessExpression.() -> Unit
): IrFunctionAccessExpression {
    return irCallConstructor(constructor).apply(block)
}

fun IrBuilder.irSetPropertyField(
    thisParam: IrValueParameter,
    property: IrProperty,
    value: IrExpression
): IrSetField {
    return irSetField(irGet(thisParam), property.backingField!!, value)
}

fun IrBuilder.irCallListOf(
    transformerScope: TransformerScope,
    type: IrType,
    elements: List<IrVarargElement>
): IrCall {
    val listOf = transformerScope
        .pluginContext
        .referenceFunctions(Kotlin.Name.listOf)
        .first { it.owner.parameters.firstOrNull()?.isVararg == true }
    return irCall(listOf) {
        arguments[0] = irVararg(elementType = type, elements = elements)
        typeArguments[0] = type
    }
}

fun IrBuilder.irVararg(elementType: IrType, elements: List<IrVarargElement>): IrVararg = IrVarargImpl(
    startOffset = startOffset,
    endOffset = endOffset,
    type = context.irBuiltIns.arrayClass.typeWith(elementType),
    varargElementType = elementType,
    elements = elements
)

fun IrBuilder.irSpread(expression: IrExpression): IrSpreadElement = IrSpreadElementImpl(
    startOffset = startOffset,
    endOffset = endOffset,
    expression = expression
)

fun IrBuilder.irMokkeryKindValue(
    enumClass: IrClass,
    kind: IrMokkeryKind
): IrExpression = irGetEnumEntry(enumClass, kind.name)

fun IrBuilder.irCallMapOf(
    transformer: TransformerScope,
    pairs: List<Pair<IrExpression, IrExpression>>,
    keyType: IrType,
    valueType: IrType
): IrCall {
    val mapOf = transformer.pluginContext
        .referenceFunctions(Kotlin.Name.mapOf)
        .first { it.owner.parameters.firstOrNull()?.isVararg == true }
    return irCall(mapOf) {
        val varargs = irVararg(
            elementType = transformer.getClass(Kotlin.Class.Pair).typeWith(keyType, valueType),
            elements = pairs.map { irCreatePair(transformer, it.first, it.second) }
        )
        typeArguments[0] = keyType
        typeArguments[1] = valueType
        arguments[0] = varargs
    }
}

private fun IrBuilder.irCreatePair(
    transformer: TransformerScope,
    first: IrExpression,
    second: IrExpression
): IrExpression {
    return irCall(transformer.getFunction(Kotlin.Function.to)) {
        typeArguments[0] = first.type
        typeArguments[1] = second.type
        arguments[0] = first
        arguments[1] = second
    }
}
