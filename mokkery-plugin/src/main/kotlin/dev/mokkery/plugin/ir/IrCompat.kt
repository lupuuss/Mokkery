@file:Suppress("UNCHECKED_CAST")

package dev.mokkery.plugin.ir

import org.jetbrains.kotlin.ir.builders.IrBuilderWithScope
import org.jetbrains.kotlin.ir.builders.irCall
import org.jetbrains.kotlin.ir.declarations.IrDeclarationOrigin
import org.jetbrains.kotlin.ir.declarations.IrFunction
import org.jetbrains.kotlin.ir.declarations.IrTypeParameter
import org.jetbrains.kotlin.ir.declarations.IrValueParameter
import org.jetbrains.kotlin.ir.expressions.IrCall
import org.jetbrains.kotlin.ir.expressions.IrExpressionBody
import org.jetbrains.kotlin.ir.expressions.IrStatementOrigin
import org.jetbrains.kotlin.ir.symbols.IrSimpleFunctionSymbol
import org.jetbrains.kotlin.ir.types.IrType
import org.jetbrains.kotlin.ir.util.copyTo
import org.jetbrains.kotlin.name.Name
import java.lang.reflect.Parameter


fun IrBuilderWithScope.irCallCompat(
    callee: IrSimpleFunctionSymbol,
    type: IrType,
    valueArgumentsCount: Int = callee.owner.valueParameters.size,
    typeArgumentsCount: Int = callee.owner.typeParameters.size,
    origin: IrStatementOrigin? = null,
): IrCall {
    return try {
        irCall(
            callee = callee,
            type = type,
            typeArgumentsCount = typeArgumentsCount,
            origin = origin,
        )
    } catch (_: NoSuchMethodError) {
        val parameterTypes = listOf(
            IrBuilderWithScope::class.java, // extension receiver
            IrSimpleFunctionSymbol::class.java, // callee
            IrType::class.java, // type
            Int::class.java, // valueArgumentsCount
            Int::class.java, // typeArgumentsCount
            IrStatementOrigin::class.java, // origin
        )
        javaClass
            .classLoader
            .loadClass("org.jetbrains.kotlin.ir.builders.ExpressionHelpersKt")
            .methods
            .single { it.name == "irCall" && it.parameters.map(Parameter::getType) == parameterTypes }
            .invoke(
                null, // static invocation
                this, // extension receiver
                callee, // param: callee
                type, // param: type
                valueArgumentsCount, // param: valueArgumentsCount
                typeArgumentsCount, // param: typeArgumentsCount
                origin, // param: origin
            ) as IrCall
    }
}

fun IrValueParameter.copyToCompat(
    irFunction: IrFunction,
    defaultValue: IrExpressionBody? = this.defaultValue,
    remapTypeMap: Map<IrTypeParameter, IrTypeParameter> = mapOf()
): IrValueParameter {
    return try {
        copyTo(
            irFunction = irFunction,
            defaultValue = defaultValue,
            remapTypeMap = remapTypeMap,
        )
    } catch (_: NoSuchMethodError) {
        val parameterTypes = listOf(
            IrValueParameter::class.java, // extension receiver
            IrFunction::class.java, // irFunction
            IrDeclarationOrigin::class.java,  // origin
            Int::class.java, // startOffset
            Int::class.java, // endOffset
            Name::class.java, // name
            Map::class.java, // remapTypeMap
            IrType::class.java, // type
            IrType::class.java, // varargElementType
            IrExpressionBody::class.java, // defaultValue
            Boolean::class.java, // isCrossinline
            Boolean::class.java, // isNoinline
            Boolean::class.java, // isAssignable
            Int::class.java, // defaults mask
            Any::class.java, // default arguments marker
        )
        javaClass
            .classLoader
            .loadClass("org.jetbrains.kotlin.ir.util.IrUtilsKt")
            .methods
            .single { it.name == "copyTo\$default" && it.parameters.map(Parameter::getType) == parameterTypes }
            .invoke(
                null, // static invocation
                this, // extension receiver
                irFunction, // param: irFunction
                null, // param: origin
                0, // param: startOffset
                0, // param: endOffset
                null, // param: name,
                remapTypeMap, // param: remapTypeMap
                null, // param: type,
                null, // param: varargElementType,
                defaultValue, // param: defaultValue
                false, // param: isCrossinline
                false, // param: isNoinline
                false, // param: isAssignable,
                0b111011011110, // defaults mask
                null // default arguments marker
            ) as IrValueParameter
    }
}

val IrCall.typeArgumentsCompat: MutableList<IrType?>
    get() = try {
        typeArguments
    } catch (e: NoSuchMethodError) {
        javaClass
            .classLoader
            .loadClass("org.jetbrains.kotlin.ir.backend.js.utils.IrJsUtilsKt")
            .methods
            .single { it.name == "getTypeArguments" }
            .invoke(
                null, // static invocation
                this, // extension receiver
            ) as MutableList<IrType?>
    }
