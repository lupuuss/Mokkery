package dev.mokkery.plugin.ext

import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.backend.jvm.ir.kClassReference
import org.jetbrains.kotlin.ir.builders.IrBuilderWithScope
import org.jetbrains.kotlin.ir.builders.irCall
import org.jetbrains.kotlin.ir.builders.irCallConstructor
import org.jetbrains.kotlin.ir.builders.irConcat
import org.jetbrains.kotlin.ir.builders.irGet
import org.jetbrains.kotlin.ir.builders.irString
import org.jetbrains.kotlin.ir.builders.irVararg
import org.jetbrains.kotlin.ir.declarations.IrClass
import org.jetbrains.kotlin.ir.declarations.IrConstructor
import org.jetbrains.kotlin.ir.declarations.IrValueParameter
import org.jetbrains.kotlin.ir.expressions.IrClassReference
import org.jetbrains.kotlin.ir.expressions.IrExpression
import org.jetbrains.kotlin.ir.expressions.IrStringConcatenation
import org.jetbrains.kotlin.ir.expressions.addArgument
import org.jetbrains.kotlin.ir.expressions.impl.IrClassReferenceImpl
import org.jetbrains.kotlin.ir.expressions.impl.IrStringConcatenationImpl
import org.jetbrains.kotlin.ir.types.IrType
import org.jetbrains.kotlin.ir.types.classifierOrFail
import org.jetbrains.kotlin.ir.util.getSimpleFunction
import org.jetbrains.kotlin.ir.util.kotlinFqName
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

fun IrBuilderWithScope.irCallHashCodeIf(irClass: IrClass) = context
    .irBuiltIns
    .anyClass
    .getSimpleFunction("hashCode")!!
    .let { irCall(it) }
    .apply {
        dispatchReceiver = irGet(irClass.thisReceiver!!)
    }


fun IrBuilderWithScope.irCallConstructor(constructor: IrConstructor) = irCallConstructor(constructor.symbol, emptyList())

fun IrBuilderWithScope.irCallMokkeryClassIdentifier(mockClass: IrClass, classToMock: IrClass): IrStringConcatenation {
    return irConcat().apply {
        addArgument(irString(classToMock.kotlinFqName.asString() + "@"))
        addArgument(irCallHashCodeIf(mockClass))
    }
}
