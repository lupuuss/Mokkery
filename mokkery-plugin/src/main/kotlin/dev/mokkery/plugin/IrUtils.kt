package dev.mokkery.plugin

import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.backend.jvm.ir.kClassReference
import org.jetbrains.kotlin.ir.builders.IrBuilderWithScope
import org.jetbrains.kotlin.ir.builders.IrGeneratorContext
import org.jetbrains.kotlin.ir.builders.declarations.IrFunctionBuilder
import org.jetbrains.kotlin.ir.builders.declarations.buildFun
import org.jetbrains.kotlin.ir.builders.declarations.buildReceiverParameter
import org.jetbrains.kotlin.ir.builders.irGet
import org.jetbrains.kotlin.ir.builders.irVararg
import org.jetbrains.kotlin.ir.declarations.IrClass
import org.jetbrains.kotlin.ir.declarations.IrDeclarationOrigin
import org.jetbrains.kotlin.ir.declarations.IrFile
import org.jetbrains.kotlin.ir.declarations.IrProperty
import org.jetbrains.kotlin.ir.declarations.IrSimpleFunction
import org.jetbrains.kotlin.ir.declarations.IrValueParameter
import org.jetbrains.kotlin.ir.declarations.path
import org.jetbrains.kotlin.ir.expressions.IrClassReference
import org.jetbrains.kotlin.ir.expressions.IrExpression
import org.jetbrains.kotlin.ir.expressions.impl.IrClassReferenceImpl
import org.jetbrains.kotlin.ir.types.IrType
import org.jetbrains.kotlin.ir.types.classifierOrFail
import org.jetbrains.kotlin.ir.types.defaultType
import org.jetbrains.kotlin.ir.types.typeWithParameters
import org.jetbrains.kotlin.ir.util.isTypeParameter
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.platform.CommonPlatforms
import org.jetbrains.kotlin.platform.konan.isNative

fun IrBuilderWithScope.kClassReferenceUnified(context: IrPluginContext, classType: IrType): IrClassReference {
    // for some reason IrClassReference is compiled differently on K/N
    // this 'if' solves the problem
    return if (context.platform!!.isNative()) {
        IrClassReferenceImpl(startOffset, endOffset, classType, classType.classifierOrFail, classType)
    } else {
        kClassReference(classType)
    }
}

fun IrBuilderWithScope.irAnyVarargParams(parameters: List<IrValueParameter>) =
    irVararg(context.irBuiltIns.anyType, parameters.map { irGet(it) })

fun IrBuilderWithScope.irVararg(values: List<IrExpression>) = irVararg(context.irBuiltIns.anyType, values)

fun IrClass.buildThisValueParam() = buildReceiverParameter(
    parent = this,
    origin = IrDeclarationOrigin.INSTANCE_RECEIVER,
    type = symbol.typeWithParameters(typeParameters)
)

inline fun IrProperty.addSetter(builder: IrFunctionBuilder.() -> Unit = {}): IrSimpleFunction =
    IrFunctionBuilder().run {
        name = Name.special("<set-${this@addSetter.name}>")
        builder()
        factory.buildFun(builder).also { setter ->
            this@addSetter.setter = setter
            setter.correspondingPropertySymbol = this@addSetter.symbol
            setter.parent = this@addSetter.parent
        }
    }

fun IrSimpleFunction.nonGenericReturnTypeOrAny(
    context: IrGeneratorContext
) = if (!returnType.isTypeParameter()) returnType else context.irBuiltIns.anyNType

fun IrExpression.locationInFile(file: IrFile): String {
    return buildString {
        append(file.path)
        append(":")
        append(file.fileEntry.getLineNumber(startOffset) + 1)
        append(":")
        append(file.fileEntry.getColumnNumber(startOffset) + 1)
    }
}
