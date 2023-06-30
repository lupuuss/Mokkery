package dev.mokkery.plugin.ext

import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.backend.common.lower.irCatch
import org.jetbrains.kotlin.backend.jvm.ir.kClassReference
import org.jetbrains.kotlin.ir.UNDEFINED_OFFSET
import org.jetbrains.kotlin.ir.builders.IrBuilderWithScope
import org.jetbrains.kotlin.ir.builders.declarations.buildVariable
import org.jetbrains.kotlin.ir.builders.irCall
import org.jetbrains.kotlin.ir.builders.irCallConstructor
import org.jetbrains.kotlin.ir.builders.irConcat
import org.jetbrains.kotlin.ir.builders.irGet
import org.jetbrains.kotlin.ir.builders.irNull
import org.jetbrains.kotlin.ir.builders.irString
import org.jetbrains.kotlin.ir.builders.irTry
import org.jetbrains.kotlin.ir.builders.irVararg
import org.jetbrains.kotlin.ir.builders.parent
import org.jetbrains.kotlin.ir.declarations.IrClass
import org.jetbrains.kotlin.ir.declarations.IrConstructor
import org.jetbrains.kotlin.ir.declarations.IrDeclarationOrigin
import org.jetbrains.kotlin.ir.declarations.IrValueParameter
import org.jetbrains.kotlin.ir.expressions.IrClassReference
import org.jetbrains.kotlin.ir.expressions.IrExpression
import org.jetbrains.kotlin.ir.expressions.IrGetEnumValue
import org.jetbrains.kotlin.ir.expressions.IrStringConcatenation
import org.jetbrains.kotlin.ir.expressions.IrTry
import org.jetbrains.kotlin.ir.expressions.addArgument
import org.jetbrains.kotlin.ir.expressions.impl.IrClassReferenceImpl
import org.jetbrains.kotlin.ir.expressions.impl.IrGetEnumValueImpl
import org.jetbrains.kotlin.ir.types.IrType
import org.jetbrains.kotlin.ir.types.classifierOrFail
import org.jetbrains.kotlin.ir.types.makeNullable
import org.jetbrains.kotlin.ir.util.defaultType
import org.jetbrains.kotlin.ir.util.getSimpleFunction
import org.jetbrains.kotlin.ir.util.kotlinFqName
import org.jetbrains.kotlin.name.Name
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

fun IrBuilderWithScope.irGetEnumEntry(irClass: IrClass, name: String): IrGetEnumValue {
    return IrGetEnumValueImpl(startOffset, endOffset, irClass.defaultType, irClass.getEnumEntry(name).symbol)
}

fun IrBuilderWithScope.irCallHashCodeIf(irClass: IrClass) = context
    .irBuiltIns
    .anyClass
    .getSimpleFunction("hashCode")!!
    .let { irCall(it) }
    .apply {
        dispatchReceiver = irGet(irClass.thisReceiver!!)
    }


fun IrBuilderWithScope.irCallConstructor(constructor: IrConstructor) =
    irCallConstructor(constructor.symbol, emptyList())

fun IrBuilderWithScope.irCallMokkeryClassIdentifier(mockClass: IrClass, classToMock: IrClass): IrStringConcatenation {
    return irConcat().apply {
        addArgument(irString(classToMock.kotlinFqName.asString() + "@"))
        addArgument(irCallHashCodeIf(mockClass))
    }
}

fun IrBuilderWithScope.irTryCatchAny(expression: IrExpression): IrTry {
    val e = buildVariable(
        parent = parent,
        startOffset = UNDEFINED_OFFSET,
        endOffset = UNDEFINED_OFFSET,
        origin = IrDeclarationOrigin.CATCH_PARAMETER,
        name = Name.identifier("e"),
        type = context.irBuiltIns.throwableType
    )
    return irTry(expression.type.makeNullable(), expression, catches = listOf(irCatch(e, irNull())), null)
}
