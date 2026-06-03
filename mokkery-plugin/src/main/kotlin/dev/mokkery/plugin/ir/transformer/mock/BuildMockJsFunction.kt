package dev.mokkery.plugin.ir.transformer.mock

import dev.mokkery.plugin.core.context.configuration
import dev.mokkery.plugin.core.ir.irBuiltIns
import dev.mokkery.plugin.core.ir.transformer.TransformerScope
import dev.mokkery.plugin.core.ir.transformer.referenced
import dev.mokkery.plugin.core.ir.transformer.referencedCompanion
import dev.mokkery.plugin.core.ir.transformer.replaceDeclarationIrBuilder
import dev.mokkery.plugin.defaultMockMode
import dev.mokkery.plugin.ir.IrMokkeryKind
import dev.mokkery.plugin.ir.MokkeryIr
import dev.mokkery.plugin.ir.irCall
import dev.mokkery.plugin.ir.irGetEnumEntry
import dev.mokkery.plugin.ir.irInvoke
import dev.mokkery.plugin.ir.irLambdaOf
import dev.mokkery.plugin.ir.kClassReference
import dev.mokkery.plugin.ir.requirePropertyGetterOwner
import dev.mokkery.plugin.ir.transformer.core.irCallListOf
import org.jetbrains.kotlin.ir.builders.createTmpVariable
import org.jetbrains.kotlin.ir.builders.irBlock
import org.jetbrains.kotlin.ir.builders.irGet
import org.jetbrains.kotlin.ir.builders.irGetObject
import org.jetbrains.kotlin.ir.builders.irNull
import org.jetbrains.kotlin.ir.builders.irReturn
import org.jetbrains.kotlin.ir.builders.irSet
import org.jetbrains.kotlin.ir.builders.irString
import org.jetbrains.kotlin.ir.declarations.IrParameterKind
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
            val extMockParam = mockFun.parameters.find { it.kind == IrParameterKind.ExtensionReceiver }
            val regularMockParams = mockFun.parameters - extMockParam
            val parentScopeValue = when (extMockParam) {
                null -> {
                    val mokkeryScopeCompanion = referencedCompanion(MokkeryIr.Class.MokkeryScope)
                    irCall(mokkeryScopeCompanion.requirePropertyGetterOwner("global")) {
                        arguments[0] = irGetObject(mokkeryScopeCompanion.symbol)
                    }
                }
                else -> expression.arguments[extMockParam]!!
            }
            val instanceScopeFun = referenced(MokkeryIr.Function.createInstanceScope)
            // initialized later to properly pass js function reference to mokkery context and context back to function
            val instanceVar = createTmpVariable(
                irExpression = irNull(),
                irType = instanceScopeFun.returnType,
                isMutable = true
            )
            val lambdaVar = createTmpVariable(
                irLambdaOf(typeToMock) {
                    +irReturn(
                        irInterceptMockCall(
                            mokkeryKind = kind,
                            mokkeryInstance = irGet(instanceVar),
                            typeParamsContainer = typeToMock.classOrFail.owner,
                            function = it
                        )
                    )
                }
            )
            val mockModeClass = referenced(MokkeryIr.Class.MockMode)
            +irSet(instanceVar, irCall(instanceScopeFun) {
                arguments[0] = parentScopeValue
                arguments[1] = irString(typeToMock.classFqName!!.asString())
                arguments[2] = kClassReference(typeToMock)
                arguments[3] = irCallListOf(
                    type = irBuiltIns.kClassClass.starProjectedType,
                    elements = typeArguments.memoryOptimizedMap { kClassReference(it) }
                )
                arguments[4] = irGet(lambdaVar)
                arguments[5] =  when (kind) {
                    IrMokkeryKind.Spy -> irNull()
                    IrMokkeryKind.Mock -> expression
                        .arguments[regularMockParams[0]!!]
                        ?: irGetEnumEntry(mockModeClass, configuration.defaultMockMode.toString())
                }
                arguments[6] = if (kind == IrMokkeryKind.Spy) expression.arguments[regularMockParams[0]!!]!! else irNull()
            })
            +irCall(referenced(MokkeryIr.Function.initializeInJsFunctionMock)) {
                arguments[0] = irGet(instanceVar)
                arguments[1] = irGet(lambdaVar)
            }
            +irCall(referenced(MokkeryIr.Function.invokeInstantiationListener)) {
                arguments[0] = irGet(instanceVar)
                arguments[1] = irGet(lambdaVar)
            }
            expression.arguments[regularMockParams[1]!!]?.let { block ->
                +irInvoke(block, false, irGet(lambdaVar))
            }
            +irGet(lambdaVar)
        }
    }
}
