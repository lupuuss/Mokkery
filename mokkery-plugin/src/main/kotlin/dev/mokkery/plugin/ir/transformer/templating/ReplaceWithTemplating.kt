package dev.mokkery.plugin.ir.transformer.templating

import dev.mokkery.plugin.context.configuration
import dev.mokkery.plugin.defaultVerifyMode
import dev.mokkery.plugin.ir.MokkeryIr
import dev.mokkery.plugin.ir.asTypeParamOrNull
import dev.mokkery.plugin.ir.defaultTypeErased
import dev.mokkery.plugin.ir.findExtensionParam
import dev.mokkery.plugin.ir.findRegularParameters
import dev.mokkery.plugin.ir.irBuiltIns
import dev.mokkery.plugin.ir.irCall
import dev.mokkery.plugin.ir.irCallConstructor
import dev.mokkery.plugin.ir.irLambdaOf
import dev.mokkery.plugin.ir.kClassReference
import dev.mokkery.plugin.ir.transformer.core.TransformerScope
import dev.mokkery.plugin.ir.transformer.core.declarationIrBuilder
import dev.mokkery.plugin.ir.transformer.core.irCallMapOf
import dev.mokkery.plugin.ir.transformer.core.irGetMokkeryScopeGlobal
import dev.mokkery.plugin.ir.transformer.core.referenced
import dev.mokkery.plugin.ir.transformer.core.referencedDefaultType
import dev.mokkery.verify.VerifyMode
import dev.mokkery.verify.VerifyModeInternals.Soft
import org.jetbrains.kotlin.backend.common.ir.moveBodyTo
import org.jetbrains.kotlin.ir.builders.IrBlockBuilder
import org.jetbrains.kotlin.ir.builders.IrBuilderWithScope
import org.jetbrains.kotlin.ir.builders.irBlock
import org.jetbrains.kotlin.ir.builders.irBoolean
import org.jetbrains.kotlin.ir.builders.irGet
import org.jetbrains.kotlin.ir.builders.irGetObject
import org.jetbrains.kotlin.ir.builders.irInt
import org.jetbrains.kotlin.ir.builders.irNull
import org.jetbrains.kotlin.ir.builders.irReturn
import org.jetbrains.kotlin.ir.builders.irString
import org.jetbrains.kotlin.ir.declarations.IrClass
import org.jetbrains.kotlin.ir.declarations.IrFunction
import org.jetbrains.kotlin.ir.declarations.IrSimpleFunction
import org.jetbrains.kotlin.ir.declarations.IrTypeParameter
import org.jetbrains.kotlin.ir.expressions.IrCall
import org.jetbrains.kotlin.ir.expressions.IrExpression
import org.jetbrains.kotlin.ir.expressions.IrFunctionExpression
import org.jetbrains.kotlin.ir.expressions.IrFunctionReference
import org.jetbrains.kotlin.ir.expressions.IrPropertyReference
import org.jetbrains.kotlin.ir.symbols.IrSimpleFunctionSymbol
import org.jetbrains.kotlin.ir.types.classOrFail
import org.jetbrains.kotlin.ir.types.makeNotNull
import org.jetbrains.kotlin.ir.types.typeWith
import org.jetbrains.kotlin.ir.util.defaultType
import org.jetbrains.kotlin.ir.util.eraseTypeParameters
import org.jetbrains.kotlin.ir.util.isVararg
import org.jetbrains.kotlin.ir.util.nestedClasses
import org.jetbrains.kotlin.ir.util.nonDispatchParameters
import org.jetbrains.kotlin.ir.util.parentAsClass
import org.jetbrains.kotlin.ir.util.primaryConstructor
import org.jetbrains.kotlin.ir.util.statements
import org.jetbrains.kotlin.utils.memoryOptimizedMap

context(scope: TransformerScope)
fun IrCall.replaceEvery(matchersCompiler: MatchersCompiler): IrExpression = replaceWithInternalEvery(
    originalCall = this,
    toBeReplacedWith = referenced(MokkeryIr.Function.internalEvery).symbol,
    matchersCompiler = matchersCompiler
)

context(scope: TransformerScope)
fun IrCall.replaceEverySuspend(matchersCompiler: MatchersCompiler) = replaceWithInternalEvery(
    originalCall = this,
    toBeReplacedWith = referenced(MokkeryIr.Function.internalEverySuspend).symbol,
    matchersCompiler = matchersCompiler
)


context(scope: TransformerScope)
fun IrCall.replaceVerify(matchersCompiler: MatchersCompiler) = replaceWithInternalVerify(
    originalCall = this,
    toBeReplacedWith = referenced(MokkeryIr.Function.internalVerify).symbol,
    matchersCompiler = matchersCompiler
)


context(scope: TransformerScope)
fun IrCall.replaceVerifySuspend(matchersCompiler: MatchersCompiler) = replaceWithInternalVerify(
    originalCall = this,
    toBeReplacedWith = referenced(MokkeryIr.Function.internalVerifySuspend).symbol,
    matchersCompiler = matchersCompiler
)


context(scope: TransformerScope)
private fun replaceWithInternalEvery(
    originalCall: IrCall,
    toBeReplacedWith: IrSimpleFunctionSymbol,
    matchersCompiler: MatchersCompiler
) = declarationIrBuilder {
    irBlock {
        +irCall(toBeReplacedWith) {
            val templatingArgument = originalCall.arguments[0]
            arguments[0] = when (templatingArgument) {
                is IrFunctionExpression -> irTemplatingLambdaFor(templatingArgument, matchersCompiler)
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
    matchersCompiler: MatchersCompiler
): IrExpression = declarationIrBuilder {
    val mokkeryScopeParam = originalCall.symbol.owner.findExtensionParam()
    val regularParams = originalCall.symbol.owner.findRegularParameters()
    val mode = originalCall.arguments[regularParams[0]]
    val block = originalCall.arguments[regularParams[1]]!!
    block as IrFunctionExpression
    irBlock {
        +irCall(toBeReplacedWith) {
            arguments[0] = mokkeryScopeParam
                ?.let(originalCall.arguments::get)
                ?: irGetMokkeryScopeGlobal()
            arguments[1] = mode ?: irGetVerifyMode(configuration.defaultVerifyMode)
            arguments[2] = irTemplatingLambdaFor(functionExpression = block, matchersCompiler = matchersCompiler)
        }
    }
}

context(scope: TransformerScope)
private fun IrBuilderWithScope.irGetVerifyMode(verifyMode: VerifyMode) = when (verifyMode) {
    is Soft -> irCallConstructor(verifyMode.toIrClass().primaryConstructor!!) {
        arguments[0] = irInt(verifyMode.atLeast)
        arguments[1] = irInt(verifyMode.atMost)
    }
    else -> irGetObject(verifyMode.toIrClass().symbol)
}

context(scope: TransformerScope)
private fun VerifyMode.toIrClass(): IrClass {
    val simpleName = this::class.simpleName
    return referenced(MokkeryIr.Class.VerifyModeInternals)
        .nestedClasses
        .find { it.name.asString() == simpleName }!!
}

context(scope: TransformerScope)
private fun IrBlockBuilder.irTemplatingLambdaFor(
    functionExpression: IrFunctionExpression,
    matchersCompiler: MatchersCompiler,
): IrFunctionExpression {
    val function = functionExpression.function
    val lambdaType = irBuiltIns
        .let { if (function.isSuspend) it.suspendFunctionN(1) else it.functionN(1) }
        .typeWith(listOf(referencedDefaultType(MokkeryIr.Class.MokkeryTemplatingScope), irBuiltIns.unitType))
    return irLambdaOf(lambdaType) { func ->
        val matchersInliningTransformer = MatchersInliningTransformer(
            pluginScope = scope,
            matchersCompiler = matchersCompiler,
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
    return irLambdaOf(lambdaType) { func ->
        +irCall(runTemplateFun) {
            typeArguments[0] = originalCall.typeArguments[0]
            arguments[0] = irGet(func.parameters[0])
            arguments[1] = dispatchReceiver
            arguments[2] = kClassReference(memberFunction.parentAsClass.defaultTypeErased)
            arguments[3] = irString(memberFunction.name.asString())
            arguments[4] = irLambdaOf(runTemplateFun.parameters[4].type.makeNotNull()) {
                val typeParameters = dispatchReceiver
                    .type
                    .classOrFail
                    .owner
                    .typeParameters
                +irReturn(irCallMapOfTemplatingParameters(memberFunction, typeParameters))
            }
            arguments[5] = irNull()
        }
    }

}

context(scope: TransformerScope)
private fun IrBuilderWithScope.irCallMapOfTemplatingParameters(
    function: IrSimpleFunction,
    parentClassTypeParameters: List<IrTypeParameter>,
): IrCall {
    val templatingParameter = referenced(MokkeryIr.Class.TemplatingParameter)
    val argMatcherClass = referenced(MokkeryIr.Class.ArgMatcher)
    val anyMatcherObject = argMatcherClass
        .nestedClasses
        .single { it.name.asString() == "Any" }
    val templatingParameterConstructor = templatingParameter.primaryConstructor!!
    return irCallMapOf(
        pairs = function.nonDispatchParameters.memoryOptimizedMap {
            val param = irCallConstructor(templatingParameterConstructor) {
                arguments[0] = irString(it.name.asString())
                arguments[1] = irBoolean(it.isVararg)
                val typeParam = it.type.asTypeParamOrNull()
                if (typeParam in parentClassTypeParameters) {
                    arguments[3] = irInt(typeParam!!.index)
                } else {
                    arguments[2] = kClassReference(it.type.eraseTypeParameters())
                }
            }
            param to irGetObject(anyMatcherObject.symbol)
        },
        keyType = templatingParameter.defaultType,
        valueType = argMatcherClass.typeWith(irBuiltIns.anyNType)
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
