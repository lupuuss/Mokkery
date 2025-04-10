package dev.mokkery.plugin.transformers

import dev.mokkery.plugin.core.IrMokkeryKind
import dev.mokkery.plugin.core.Mokkery
import dev.mokkery.plugin.core.TransformerScope
import dev.mokkery.plugin.core.declarationIrBuilder
import dev.mokkery.plugin.core.getClass
import dev.mokkery.plugin.core.getFunction
import dev.mokkery.plugin.core.mockMode
import dev.mokkery.plugin.ir.eraseTypeParametersCompat
import dev.mokkery.plugin.ir.irCall
import dev.mokkery.plugin.ir.irCallListOf
import dev.mokkery.plugin.ir.irGetEnumEntry
import dev.mokkery.plugin.ir.irInvoke
import dev.mokkery.plugin.ir.irLambda
import dev.mokkery.plugin.ir.irMokkeryKindValue
import dev.mokkery.plugin.ir.kClassReference
import org.jetbrains.kotlin.ir.backend.js.utils.valueArguments
import org.jetbrains.kotlin.ir.builders.IrBuilderWithScope
import org.jetbrains.kotlin.ir.builders.createTmpVariable
import org.jetbrains.kotlin.ir.builders.irBlock
import org.jetbrains.kotlin.ir.builders.irGet
import org.jetbrains.kotlin.ir.builders.irGetObject
import org.jetbrains.kotlin.ir.builders.irNull
import org.jetbrains.kotlin.ir.builders.irReturn
import org.jetbrains.kotlin.ir.builders.irString
import org.jetbrains.kotlin.ir.expressions.IrCall
import org.jetbrains.kotlin.ir.expressions.IrExpression
import org.jetbrains.kotlin.ir.types.IrSimpleType
import org.jetbrains.kotlin.ir.types.classFqName
import org.jetbrains.kotlin.ir.types.classOrFail
import org.jetbrains.kotlin.ir.types.starProjectedType
import org.jetbrains.kotlin.ir.types.typeOrFail
import org.jetbrains.kotlin.utils.memoryOptimizedMap

fun TransformerScope.buildMockJsFunction(
    expression: IrCall,
    kind: IrMokkeryKind
): IrExpression {
    val typeToMock = expression.type
    val typeArguments = typeToMock.let { it as IrSimpleType }
        .arguments
        .map { it.typeOrFail.eraseTypeParametersCompat() }
    val returnType = typeArguments.last()
    val transformer = this
    return declarationIrBuilder(expression) {
        irBlock {
            val modeArg = irMockModeArg(transformer, expression, kind)
            val parentScope = expression
                .extensionReceiver
                ?: irGetObject(transformer.getClass(Mokkery.Class.GlobalMokkeryScope).symbol)
            val instanceScopeFun = transformer.getFunction(Mokkery.Function.MokkeryInstanceScope)
            val mokkeryInstanceCall = irCall(instanceScopeFun) {
                putValueArgument(0, parentScope)
                putValueArgument(1, modeArg)
                putValueArgument(2, irMokkeryKindValue(transformer.getClass(Mokkery.Class.MokkeryKind), kind))
                putValueArgument(3, irString(typeToMock.classFqName!!.asString()))
                putValueArgument(4, kClassReference(typeToMock))
                putValueArgument(
                    index = 5,
                    valueArgument = irCallListOf(
                        transformerScope = transformer,
                        type = context.irBuiltIns.kClassClass.starProjectedType,
                        expressions = typeArguments.memoryOptimizedMap { kClassReference(it) }
                    )
                )
                putValueArgument(6, if (kind == IrMokkeryKind.Spy) expression.valueArguments[0]!! else irNull())
            }
            val instanceVar = createTmpVariable(mokkeryInstanceCall)
            val lambda = irLambda(returnType, typeToMock, currentFile) {
                +irReturn(
                    irInterceptCall(
                        transformer = transformer,
                        mokkeryKind = kind,
                        mokkeryInstance = irGet(instanceVar),
                        typeParamsContainer = typeToMock.classOrFail.owner,
                        function = it
                    )
                )
            }
            val lambdaVar = createTmpVariable(lambda)
            +irCall(transformer.getFunction(Mokkery.Function.invokeInstantiationListener)) {
                this.extensionReceiver = irGet(instanceVar)
                putValueArgument(0, irGet(lambdaVar))
            }
            val block = expression.valueArguments.getOrNull(1)
            if (block != null) {
                +irInvoke(block, false, irGet(lambdaVar))
            }
            +irGet(lambdaVar)
        }
    }
}

private fun IrBuilderWithScope.irMockModeArg(
    transformer: TransformerScope,
    expression: IrCall,
    kind: IrMokkeryKind
): IrExpression {
    val mockModeClass = transformer.getClass(Mokkery.Class.MockMode)
    return when (kind) {
        IrMokkeryKind.Spy -> irGetEnumEntry(mockModeClass, "strict")
        IrMokkeryKind.Mock -> expression
            .valueArguments
            .getOrNull(0)
            ?: irGetEnumEntry(mockModeClass, transformer.mockMode.toString())
    }
}
