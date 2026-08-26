package dev.mokkery.plugin.ir.transformer.templating

import dev.mokkery.plugin.core.ir.irBuiltIns
import dev.mokkery.plugin.core.ir.transformer.TransformerScope
import dev.mokkery.plugin.core.ir.transformer.referenced
import dev.mokkery.plugin.core.ir.transformer.referencedDefaultType
import dev.mokkery.plugin.core.ir.transformer.replaceDeclarationIrBuilder
import dev.mokkery.plugin.ir.MokkeryIr
import dev.mokkery.plugin.ir.defaultTypeErased
import dev.mokkery.plugin.ir.findRegularParameters
import dev.mokkery.plugin.ir.hasNonDispatchParameters
import dev.mokkery.plugin.ir.irCall
import dev.mokkery.plugin.ir.irLambdaOf
import dev.mokkery.plugin.ir.kClassReference
import dev.mokkery.plugin.ir.mokkeryFunctionId
import dev.mokkery.plugin.ir.transformer.core.irCallListOf
import dev.mokkery.plugin.ir.transformer.core.irGetMokkeryModuleScope
import dev.mokkery.plugin.ir.transformer.core.irGetMokkeryScopeFor
import org.jetbrains.kotlin.backend.common.ir.moveBodyTo
import org.jetbrains.kotlin.descriptors.Modality
import org.jetbrains.kotlin.ir.builders.IrBlockBuilder
import org.jetbrains.kotlin.ir.builders.IrBuilderWithScope
import org.jetbrains.kotlin.ir.builders.irBlock
import org.jetbrains.kotlin.ir.builders.irGet
import org.jetbrains.kotlin.ir.builders.irGetObject
import org.jetbrains.kotlin.ir.builders.irLong
import org.jetbrains.kotlin.ir.builders.irNull
import org.jetbrains.kotlin.ir.builders.irReturn
import org.jetbrains.kotlin.ir.builders.irString
import org.jetbrains.kotlin.ir.declarations.IrFunction
import org.jetbrains.kotlin.ir.declarations.IrSimpleFunction
import org.jetbrains.kotlin.ir.expressions.IrCall
import org.jetbrains.kotlin.ir.expressions.IrExpression
import org.jetbrains.kotlin.ir.expressions.IrFunctionExpression
import org.jetbrains.kotlin.ir.expressions.IrFunctionReference
import org.jetbrains.kotlin.ir.expressions.IrPropertyReference
import org.jetbrains.kotlin.ir.symbols.IrSimpleFunctionSymbol
import org.jetbrains.kotlin.ir.types.makeNotNull
import org.jetbrains.kotlin.ir.types.typeWith
import org.jetbrains.kotlin.ir.util.nestedClasses
import org.jetbrains.kotlin.ir.util.nonDispatchParameters
import org.jetbrains.kotlin.ir.util.parentAsClass
import org.jetbrains.kotlin.ir.util.statements
import org.jetbrains.kotlin.ir.util.substitute
import org.jetbrains.kotlin.utils.memoryOptimizedMap

context(scope: TransformerScope)
fun IrCall.replaceEvery(): IrExpression = replaceWithInternalEvery(
    originalCall = this,
    toBeReplacedWith = referenced(MokkeryIr.Function.internalEvery).symbol,
)

context(scope: TransformerScope)
fun IrCall.replaceEverySuspend() = replaceWithInternalEvery(
    originalCall = this,
    toBeReplacedWith = referenced(MokkeryIr.Function.internalEverySuspend).symbol,
)


context(scope: TransformerScope)
fun IrCall.replaceVerify() = replaceWithInternalVerify(
    originalCall = this,
    toBeReplacedWith = referenced(MokkeryIr.Function.internalVerify).symbol,
)


context(scope: TransformerScope)
fun IrCall.replaceVerifySuspend() = replaceWithInternalVerify(
    originalCall = this,
    toBeReplacedWith = referenced(MokkeryIr.Function.internalVerifySuspend).symbol,
)


context(scope: TransformerScope)
private fun replaceWithInternalEvery(
    originalCall: IrCall,
    toBeReplacedWith: IrSimpleFunctionSymbol,
) = originalCall.replaceDeclarationIrBuilder {
    irBlock {
        +irCall(toBeReplacedWith) {
            val templatingArgument = originalCall.arguments[0]
            arguments[0] = irGetMokkeryModuleScope()
            arguments[1] = when (templatingArgument) {
                is IrFunctionExpression -> irTemplatingLambdaFor(templatingArgument)
                is IrFunctionReference -> irTemplatingLambdaFor(templatingArgument, originalCall)
                else -> error("Unsupported templating argument!")
            }
            typeArguments[0] = originalCall.typeArguments[0]
        }
    }
}

context(scope: TransformerScope)
private fun replaceWithInternalVerify(
    originalCall: IrCall,
    toBeReplacedWith: IrSimpleFunctionSymbol,
): IrExpression = originalCall.replaceDeclarationIrBuilder {
    val regularParams = originalCall.symbol.owner.findRegularParameters()
    val mode = originalCall.arguments[regularParams[0]]
    val block = originalCall.arguments[regularParams[1]]!!
    block as IrFunctionExpression
    irBlock {
        +irCall(toBeReplacedWith) {
            arguments[0] = irGetMokkeryScopeFor(originalCall)
            arguments[1] = mode ?: irNull()
            arguments[2] = irTemplatingLambdaFor(functionExpression = block)
        }
    }
}

context(scope: TransformerScope)
private fun IrBlockBuilder.irTemplatingLambdaFor(
    functionExpression: IrFunctionExpression,
): IrFunctionExpression {
    val function = functionExpression.function
    val lambdaType = irBuiltIns
        .let { if (function.isSuspend) it.suspendFunctionN(1) else it.functionN(1) }
        .typeWith(listOf(referencedDefaultType(MokkeryIr.Class.MokkeryTemplatingScope), irBuiltIns.unitType))
    return irLambdaOf(lambdaType) { func ->
        val matchersInliningTransformer = MatchersInliningTransformer(
            pluginScope = scope,
            initialValueDeclarations = emptyList()
        )
        val templatingTransformer = TemplatingTransformer(
            pluginScope = scope,
            templatingScopeParam = func.parameters[0],
        )
        val templatingCleanupTransformer = TemplatingCleanupTransformer(scope, function.symbol)
        val newBody = function
            .transform(matchersInliningTransformer, null)
            .transform(templatingTransformer, null)
            .transform(templatingCleanupTransformer, null)
            .let { it as IrFunction }
            .moveBodyTo(func, mapOf(function.parameters[0] to func.parameters[0]))
        newBody?.statements?.unaryPlus()
    }
}


context(scope: TransformerScope)
private fun IrBuilderWithScope.irTemplatingLambdaFor(
    referenceExpression: IrFunctionReference,
    originalCall: IrCall,
): IrFunctionExpression {
    val (dispatchReceiver, memberFunction) = referenceExpression.extractDispatchReceiverAndMemberFunction()
    val lambdaType = irBuiltIns
        .let { if (memberFunction.isSuspend) it.suspendFunctionN(1) else it.functionN(1) }
        .typeWith(listOf(referencedDefaultType(MokkeryIr.Class.MokkeryTemplatingScope), irBuiltIns.unitType))
    val runTemplateFun = when {
        memberFunction.isSuspend -> referenced(MokkeryIr.Function.runTemplateSuspend)
        else -> referenced(MokkeryIr.Function.runTemplate)
    }
    val resultType = originalCall.typeArguments[0] ?: irBuiltIns.anyNType
    val substitution = mapOf(runTemplateFun.typeParameters[0].symbol to resultType)
    return irLambdaOf(lambdaType) { func ->
        +irCall(runTemplateFun, runTemplateFun.returnType.substitute(substitution)) {
            typeArguments[0] = originalCall.typeArguments[0]
            arguments[0] = irGet(func.parameters[0])
            arguments[1] = when (memberFunction.modality) {
                Modality.FINAL -> irCallCheckMockFinalMemberCall(dispatchReceiver, memberFunction)
                else -> dispatchReceiver
            }
            arguments[2] = kClassReference(memberFunction.parentAsClass.defaultTypeErased)
            arguments[3] = irLong(memberFunction.mokkeryFunctionId)
            arguments[4] = irString(memberFunction.name.asString())
            arguments[5] = when {
                !memberFunction.hasNonDispatchParameters() -> irNull()
                else -> irLambdaOf(runTemplateFun.parameters[5].type.makeNotNull()) {
                    +irReturn(irCallListOfAnyMatchers(memberFunction))
                }
            }
            arguments[6] = irNull()
        }
    }

}

context(scope: TransformerScope)
private fun IrBuilderWithScope.irCallCheckMockFinalMemberCall(
    receiver: IrExpression,
    memberFunction: IrSimpleFunction,
): IrExpression = irCall(referenced(MokkeryIr.Function.checkMockFinalMemberCall), receiver.type) {
    arguments[0] = receiver
    arguments[1] = irString(memberFunction.name.asString())
    typeArguments[0] = receiver.type
}

context(scope: TransformerScope)
private fun IrBuilderWithScope.irCallListOfAnyMatchers(function: IrSimpleFunction): IrCall {
    val argMatcherClass = referenced(MokkeryIr.Class.ArgMatcher)
    val anyMatcherObject = argMatcherClass
        .nestedClasses
        .single { it.name.asString() == "Any" }
    return irCallListOf(
        type = argMatcherClass.typeWith(irBuiltIns.anyNType),
        elements = function.nonDispatchParameters.memoryOptimizedMap { irGetObject(anyMatcherObject.symbol) }
    )
}

private fun IrFunctionReference.extractDispatchReceiverAndMemberFunction(): Pair<IrExpression, IrSimpleFunction> {
    // it handles 2 cases:
    // * this::property::get/this::property::set
    // * this::function
    return when (val dispatchArgument = arguments[0]!!) {
        is IrPropertyReference -> dispatchArgument.arguments[0]!! to when {
            symbol.owner.name.asString() == "get" -> dispatchArgument.getter!!.owner
            symbol.owner.name.asString() == "set" -> dispatchArgument.setter!!.owner
            else -> error("Only reference to getter and setter is allowed on property!")
        }
        else -> dispatchArgument to symbol.owner as IrSimpleFunction
    }
}
