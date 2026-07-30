package dev.mokkery.plugin.ir.transformer.mock

import dev.mokkery.plugin.core.ir.irBuiltIns
import dev.mokkery.plugin.core.ir.transformer.TransformerScope
import dev.mokkery.plugin.core.ir.transformer.referenced
import dev.mokkery.plugin.core.ir.transformer.referencedGetter
import dev.mokkery.plugin.core.ir.transformer.replaceDeclarationIrBuilder
import dev.mokkery.plugin.ir.IrMokkeryKind
import dev.mokkery.plugin.ir.MokkeryIr
import dev.mokkery.plugin.ir.findRegularParameters
import dev.mokkery.plugin.ir.irCall
import dev.mokkery.plugin.ir.irLambdaOf
import dev.mokkery.plugin.ir.kClassReference
import dev.mokkery.plugin.ir.transformer.core.irCallListOf
import dev.mokkery.plugin.ir.transformer.core.irGetMokkeryScopeFor
import org.jetbrains.kotlin.ir.builders.createTmpVariable
import org.jetbrains.kotlin.ir.builders.irBlock
import org.jetbrains.kotlin.ir.builders.irGet
import org.jetbrains.kotlin.ir.builders.irNull
import org.jetbrains.kotlin.ir.builders.irReturn
import org.jetbrains.kotlin.ir.builders.irSet
import org.jetbrains.kotlin.ir.builders.irString
import org.jetbrains.kotlin.ir.expressions.IrCall
import org.jetbrains.kotlin.ir.expressions.IrExpression
import org.jetbrains.kotlin.ir.types.IrSimpleType
import org.jetbrains.kotlin.ir.types.classFqName
import org.jetbrains.kotlin.ir.types.classOrFail
import org.jetbrains.kotlin.ir.types.starProjectedType
import org.jetbrains.kotlin.ir.types.typeOrFail
import org.jetbrains.kotlin.ir.util.eraseTypeParameters
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
            val lambdaVar = createTmpVariable(
                irLambdaOf(typeToMock) {
                    val scopeGetter = referencedGetter(MokkeryIr.Property.jsFunctionMokkeryScope)
                    +irReturn(
                        irInterceptMockCall(
                            mokkeryKind = kind,
                            mokkeryInstance = { irCall(scopeGetter) { arguments[0] = irGet(self) } },
                            typeParamsContainer = typeToMock.classOrFail.owner,
                            function = it
                        )
                    )
                }
            )
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
                arguments[5] =  when (kind) {
                    IrMokkeryKind.Spy -> irNull()
                    IrMokkeryKind.Mock -> expression.arguments[regularMockParams[0]] ?: irNull()
                }
                arguments[6] = if (kind == IrMokkeryKind.Spy) expression.arguments[regularMockParams[0]]!! else irNull()
                arguments[7] = expression.arguments[regularMockParams[1]] ?: irNull()
            }
            +irGet(lambdaVar)
        }
    }
}
