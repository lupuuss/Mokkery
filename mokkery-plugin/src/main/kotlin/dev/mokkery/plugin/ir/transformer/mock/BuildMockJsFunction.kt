package dev.mokkery.plugin.ir.transformer.mock

import dev.mokkery.plugin.core.ir.irBuiltIns
import dev.mokkery.plugin.core.ir.transformer.TransformerScope
import dev.mokkery.plugin.core.ir.transformer.referenced
import dev.mokkery.plugin.core.ir.transformer.referencedGetter
import dev.mokkery.plugin.core.ir.transformer.referencedPrimaryConstructor
import dev.mokkery.plugin.core.ir.transformer.replaceDeclarationIrBuilder
import dev.mokkery.plugin.ir.IrMokkeryKind
import dev.mokkery.plugin.ir.MokkeryIr
import dev.mokkery.plugin.ir.defaultTypeErased
import dev.mokkery.plugin.ir.findRegularParameters
import dev.mokkery.plugin.ir.irCall
import dev.mokkery.plugin.ir.irCallConstructor
import dev.mokkery.plugin.ir.irInvoke
import dev.mokkery.plugin.ir.irLambdaOf
import dev.mokkery.plugin.ir.kClassReference
import dev.mokkery.plugin.ir.transformer.core.irCallListGet
import dev.mokkery.plugin.ir.transformer.core.irCallListOf
import dev.mokkery.plugin.ir.transformer.core.irGetMokkeryScopeFor
import org.jetbrains.kotlin.ir.builders.IrBuilderWithScope
import org.jetbrains.kotlin.ir.builders.createTmpVariable
import org.jetbrains.kotlin.ir.builders.irBlock
import org.jetbrains.kotlin.ir.builders.irGet
import org.jetbrains.kotlin.ir.builders.irNull
import org.jetbrains.kotlin.ir.builders.irReturn
import org.jetbrains.kotlin.ir.builders.irSet
import org.jetbrains.kotlin.ir.builders.irString
import org.jetbrains.kotlin.ir.declarations.IrSimpleFunction
import org.jetbrains.kotlin.ir.expressions.IrCall
import org.jetbrains.kotlin.ir.expressions.IrExpression
import org.jetbrains.kotlin.ir.types.IrSimpleType
import org.jetbrains.kotlin.ir.types.IrType
import org.jetbrains.kotlin.ir.types.classFqName
import org.jetbrains.kotlin.ir.types.classOrFail
import org.jetbrains.kotlin.ir.types.starProjectedType
import org.jetbrains.kotlin.ir.types.typeOrFail
import org.jetbrains.kotlin.ir.types.typeWith
import org.jetbrains.kotlin.ir.util.eraseTypeParameters
import org.jetbrains.kotlin.ir.util.isSuspend
import org.jetbrains.kotlin.utils.memoryOptimizedMap

context(scope: TransformerScope)
fun buildMockJsFunction(
    expression: IrCall,
    kind: IrMokkeryKind
): IrExpression {
    val typeToMock = expression.type
    val typeArguments = typeToMock.let { it as IrSimpleType }
        .arguments
        .map { it.typeOrFail.eraseTypeParameters() }
    return expression.replaceDeclarationIrBuilder {
        irBlock {
            val mockFun = expression.symbol.owner
            val regularMockParams = mockFun.findRegularParameters()
            val setupInstanceScopeFun = referenced(MokkeryIr.Function.setupMokkeryInstanceForJsFunction)
            val self = createTmpVariable(
                irExpression = irNull(),
                irType = typeToMock,
                isMutable = true
            )
            val spiedVar = when (kind) {
                IrMokkeryKind.Spy -> createTmpVariable(expression.arguments[regularMockParams[0]]!!)
                IrMokkeryKind.Mock -> null
            }
            val lambda = irLambdaOf(typeToMock) {
                val scopeGetter = referencedGetter(MokkeryIr.Property.jsFunctionMokkeryScope)
                +irReturn(
                    irInterceptMockCall(
                        mokkeryInstance = { irCall(scopeGetter) { arguments[0] = irGet(self) } },
                        typeParamsContainer = typeToMock.classOrFail.owner,
                        function = it,
                        functionId = 0
                    )
                )
            }
            val lambdaVar = createTmpVariable(lambda)
            +irSet(self, irGet(lambdaVar))
            +irCall(setupInstanceScopeFun) {
                arguments[0] = irGet(lambdaVar)
                arguments[1] = irGetMokkeryScopeFor(expression)
                arguments[2] = irString(typeToMock.classFqName!!.asString())
                arguments[3] = kClassReference(typeToMock)
                arguments[4] = irCallListOf(
                    type = irBuiltIns.kClassClass.starProjectedType,
                    elements = typeArguments.memoryOptimizedMap { kClassReference(it) }
                )
                arguments[5] = when (kind) {
                    IrMokkeryKind.Spy -> irNull()
                    IrMokkeryKind.Mock -> expression.arguments[regularMockParams[0]] ?: irNull()
                }
                arguments[6] = spiedVar?.let(::irGet) ?: irNull()
                arguments[7] = spiedVar
                    ?.let { irLambdaSpyCallDispatcher(irGet(it), lambda.function) }
                    ?: irNull()
                arguments[8] = expression.arguments[regularMockParams[1]] ?: irNull()
            }
            +irGet(lambdaVar)
        }
    }
}

context(scope: TransformerScope)
private fun IrBuilderWithScope.irLambdaSpyCallDispatcher(
    spiedLambda: IrExpression,
    function: IrSimpleFunction,
): IrExpression {
    val adapter = irLambdaOf(function.dispatchLambdaType()) { lambda ->
        val args = Array(function.parameters.size) { irCallListGet(irGet(lambda.parameters[0]), it) }
        +irReturn(
            irInvoke(
                function = spiedLambda,
                isSuspend = lambda.isSuspend,
                args = args,
                returnType = function.returnType
            )
        )
    }
    return irCallConstructor(referencedPrimaryConstructor(MokkeryIr.Class.LambdaSpyCallDispatcher)) {
        arguments[0] = if (function.isSuspend) irNull() else adapter
        arguments[1] = if (function.isSuspend) adapter else irNull()
    }
}

context(scope: TransformerScope)
private fun IrSimpleFunction.dispatchLambdaType(): IrType {
    val lambdaClass = if (isSuspend) irBuiltIns.suspendFunctionN(1) else irBuiltIns.functionN(1)
    return lambdaClass.typeWith(irBuiltIns.listClass.owner.defaultTypeErased, returnType)
}
