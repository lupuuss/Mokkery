package dev.mokkery.plugin.ir.transformer.templating

import dev.mokkery.plugin.Mokkery
import dev.mokkery.plugin.core.ir.IrMokkeryPluginScope
import dev.mokkery.plugin.core.ir.irBuiltIns
import dev.mokkery.plugin.core.ir.transformer.CoreTransformer
import dev.mokkery.plugin.core.ir.transformer.referenced
import dev.mokkery.plugin.core.ir.transformer.referencedPrimaryConstructor
import dev.mokkery.plugin.core.ir.transformer.replaceDeclarationIrBuilder
import dev.mokkery.plugin.ir.MokkeryIr
import dev.mokkery.plugin.ir.applyTransformChildrenVoid
import dev.mokkery.plugin.ir.defaultTypeErased
import dev.mokkery.plugin.ir.hasNonDispatchParameters
import dev.mokkery.plugin.ir.irCall
import dev.mokkery.plugin.ir.irCallConstructor
import dev.mokkery.plugin.ir.irLambdaOf
import dev.mokkery.plugin.ir.kClassReference
import dev.mokkery.plugin.ir.mokkeryFunctionId
import dev.mokkery.plugin.ir.transformer.core.irCallEqMatcher
import dev.mokkery.plugin.ir.transformer.core.irCallListGet
import dev.mokkery.plugin.ir.transformer.core.irCallListOf
import org.jetbrains.kotlin.backend.common.ir.inline
import org.jetbrains.kotlin.builtins.StandardNames.BUILT_INS_PACKAGE_FQ_NAME
import org.jetbrains.kotlin.descriptors.Modality
import org.jetbrains.kotlin.ir.builders.IrBlockBodyBuilder
import org.jetbrains.kotlin.ir.builders.IrBuilderWithScope
import org.jetbrains.kotlin.ir.builders.createTmpVariable
import org.jetbrains.kotlin.ir.builders.irAs
import org.jetbrains.kotlin.ir.builders.irBlock
import org.jetbrains.kotlin.ir.builders.irBoolean
import org.jetbrains.kotlin.ir.builders.irGet
import org.jetbrains.kotlin.ir.builders.irLong
import org.jetbrains.kotlin.ir.builders.irNull
import org.jetbrains.kotlin.ir.builders.irReturn
import org.jetbrains.kotlin.ir.builders.irString
import org.jetbrains.kotlin.ir.builders.parent
import org.jetbrains.kotlin.ir.declarations.IrSimpleFunction
import org.jetbrains.kotlin.ir.declarations.IrValueParameter
import org.jetbrains.kotlin.ir.declarations.IrVariable
import org.jetbrains.kotlin.ir.expressions.IrCall
import org.jetbrains.kotlin.ir.expressions.IrExpression
import org.jetbrains.kotlin.ir.expressions.IrFunctionExpression
import org.jetbrains.kotlin.ir.expressions.IrSpreadElement
import org.jetbrains.kotlin.ir.expressions.IrVararg
import org.jetbrains.kotlin.ir.expressions.IrVarargElement
import org.jetbrains.kotlin.ir.expressions.impl.IrSpreadElementImpl
import org.jetbrains.kotlin.ir.types.IrType
import org.jetbrains.kotlin.ir.types.classOrNull
import org.jetbrains.kotlin.ir.types.getClass
import org.jetbrains.kotlin.ir.types.makeNotNull
import org.jetbrains.kotlin.ir.types.typeWith
import org.jetbrains.kotlin.ir.util.deepCopyWithSymbols
import org.jetbrains.kotlin.ir.util.dumpKotlinLike
import org.jetbrains.kotlin.ir.util.isFinalClass
import org.jetbrains.kotlin.ir.util.isSubclassOf
import org.jetbrains.kotlin.ir.util.isVararg
import org.jetbrains.kotlin.ir.util.kotlinFqName
import org.jetbrains.kotlin.ir.util.nonDispatchParameters
import org.jetbrains.kotlin.ir.util.parentAsClass
import org.jetbrains.kotlin.ir.util.patchDeclarationParents
import org.jetbrains.kotlin.ir.util.substitute
import org.jetbrains.kotlin.ir.util.typeSubstitutionMap
import org.jetbrains.kotlin.ir.visitors.IrElementTransformerVoid
import org.jetbrains.kotlin.utils.memoryOptimizedMap

class TemplatingTransformer(
    pluginScope: IrMokkeryPluginScope,
    private val templatingScopeParam: IrValueParameter,
) : CoreTransformer(pluginScope) {

    private val argMatcherClass = referenced(MokkeryIr.Class.ArgMatcher)
    private val defaultValuesMatcherConstructor = referencedPrimaryConstructor(MokkeryIr.Class.DefaultValuesMatcher)
    private val runTemplateBlockingFun = referenced(MokkeryIr.Function.runTemplate)
    private val runTemplateSuspendFun = referenced(MokkeryIr.Function.runTemplateSuspend)
    private val inlineLiteralsAsMatchersFun = referenced(MokkeryIr.Function.inlineLiteralsAsMatchers)

    private val contextFunctions = setOf(Mokkery.Name.ext, Mokkery.Name.ctx)

    override fun visitCall(expression: IrCall): IrExpression {
        if (expression.symbol.owner.kotlinFqName in contextFunctions) {
            return inlineContextFunction(expression).applyTransformChildrenVoid()
        }
        if (expression.type.isMatcher()) return expression
        val receiver = expression.dispatchReceiver
        val cls = receiver?.type?.getClass()
        if (receiver == null || cls?.isFinalClass == true) return expression.applyTransformChildrenVoid()
        if (expression.symbol.owner.modality == Modality.FINAL) {
            return expression.wrapDispatchersWithMockCheck(referenced(MokkeryIr.Function.checkMockFinalMemberCall))
        }
        if (receiver is IrCall) receiver.transformChildrenVoid()
        val functionToReplace = expression.symbol.owner
        val runTemplateFun = if (functionToReplace.isSuspend) runTemplateSuspendFun else runTemplateBlockingFun
        val substitution = mapOf(runTemplateFun.typeParameters[0].symbol to expression.type)
        return expression.replaceDeclarationIrBuilder {
            irBlock(resultType = runTemplateFun.returnType.substitute(substitution)) {
                val receiverVar = createTmpVariable(receiver)
                +irCall(runTemplateFun, runTemplateFun.returnType.substitute(substitution)) {
                    typeArguments[0] = expression.type
                    arguments[0] = irGet(templatingScopeParam)
                    arguments[1] = irGet(receiverVar)
                    arguments[2] = kClassReference(functionToReplace.parentAsClass.defaultTypeErased)
                    arguments[3] = irLong(functionToReplace.mokkeryFunctionId)
                    arguments[4] = irString(functionToReplace.name.asString())
                    arguments[5] = when {
                        !functionToReplace.hasNonDispatchParameters() -> irNull()
                        else -> irLambdaOf(runTemplateFun.parameters[5].type.makeNotNull()) {
                            createTemplatingArgumentsLambdaBody(it, expression)
                        }
                    }
                    arguments[6] = when {
                        expression.usesMatchers -> irNull()
                        else -> irLambdaOf(runTemplateFun.parameters[6].type.makeNotNull().substitute(substitution)) {
                            val original = expression.deepCopyWithSymbols(initialParent = it)
                            original.arguments[0] = irGet(receiverVar)
                            +irReturn(original)
                        }
                    }
                }
            }
        }
    }

    private fun inlineContextFunction(call: IrCall) = call.replaceDeclarationIrBuilder {
        val callArguments = call.arguments
        val blockParam = callArguments.last() as IrFunctionExpression
        irBlock {
            val variables = callArguments
                .subList(1, callArguments.lastIndex)
                .memoryOptimizedMap { createTmpVariable(it!!) }
            +blockParam.function.inline(parent, variables)
        }
    }

    private fun IrBlockBodyBuilder.createTemplatingArgumentsLambdaBody(lambda: IrSimpleFunction, expression: IrCall) {
        val calledFunc = expression.symbol.owner
        val hasDefaults = expression.arguments.any { it == null }
        val defaultsMatcherVar = when {
            hasDefaults -> createTmpVariable(irCallDefaultValuesMatcherContractorFor(expression))
            else -> null
        }
        +irReturn(
            irCallListOf(
                type = argMatcherClass.typeWith(irBuiltIns.anyNType),
                elements = calledFunc.nonDispatchParameters.memoryOptimizedMap {
                    val argument = expression
                        .arguments[it]
                        ?.wrapDispatchersWithMockCheck(referenced(MokkeryIr.Function.checkMockMemberCallResultAccess))
                        ?.patchDeclarationParents(lambda)
                    replaceTopTemplatingArg(argument, it, defaultsMatcherVar)
                }
            )
        )
    }

    private val IrCall.usesMatchers: Boolean
        get() = arguments.any { it?.type?.isMatcher() == true || (it is IrVararg && it.varargElementType.isMatcher()) }

    private fun IrBuilderWithScope.replaceTopTemplatingArg(
        arg: IrExpression?,
        param: IrValueParameter,
        defaultValueMatcherVar: IrVariable?,
    ): IrExpression = when {
        param.isVararg -> replaceVararg(arg)
        arg == null -> irGet(defaultValueMatcherVar!!)
        arg.type.isMatcher() -> arg
        else -> irCallEqMatcher(arg, irBuiltIns.anyNType)
    }

    // the receiver is not necessarily a call - nested calls have to be found in any expression,
    // for instance in a string concatenation or in vararg elements
    private fun IrExpression.wrapDispatchersWithMockCheck(func: IrSimpleFunction): IrExpression = transform(
        transformer = object : IrElementTransformerVoid() {
            override fun visitCall(expression: IrCall) = expression.transformPostfix {
                val dispatcher = dispatchReceiver ?: return@transformPostfix
                dispatchReceiver = dispatcher.replaceDeclarationIrBuilder {
                    irCall(
                        func = func,
                        type = dispatcher.type
                    ) {
                        arguments[0] = dispatcher
                        arguments[1] = irString(expression.symbol.owner.name.asString())
                        typeArguments[0] = dispatcher.type
                    }
                }
            }
        },
        data = null
    )

    private fun IrBuilderWithScope.replaceVararg(expression: IrExpression?): IrExpression {
        if (expression == null) return compositeVarargMatcher(emptyList())
        if (expression !is IrVararg) {
            error("Expected vararg expression but received ${expression.dumpKotlinLike()}!")
        }
        if (expression.varargElementType.type.isMatcher()) {
            return compositeVarargMatcher(expression.elements.map { it })
        }
        val singleArraySpread = expression.extractSingleArraySpreadOfMatchers()
        if (singleArraySpread != null) {
            return compositeVarargMatcher(singleArraySpread.elements.map { it })
        }
        return compositeVarargMatcher(
            expression.elements.map {
                when (it) {
                    is IrSpreadElement -> spreadLiteralsAsMatchers(it)
                    else -> irCallEqMatcher(it as IrExpression, irBuiltIns.anyNType)
                }
            }
        )
    }

    private fun IrBuilderWithScope.spreadLiteralsAsMatchers(spread: IrSpreadElement): IrSpreadElement {
        return IrSpreadElementImpl(
            startOffset = startOffset,
            endOffset = endOffset,
            expression = irCall(inlineLiteralsAsMatchersFun) {
                arguments[0] = spread.expression
            }
        )
    }

    private fun IrBuilderWithScope.compositeVarargMatcher(
        matchers: List<IrVarargElement>
    ) = irCallConstructor(referencedPrimaryConstructor(MokkeryIr.Class.CompositeVarArgMatcher)) {
        arguments[0] = irCallListOf(
            type = argMatcherClass.typeWith(irBuiltIns.anyNType),
            elements = matchers
        )
    }

    private fun IrType.isMatcher(): Boolean {
        val classSymbol = classOrNull ?: return false
        val clazz = classSymbol.owner
        if (clazz == argMatcherClass) return true
        return clazz.isSubclassOf(argMatcherClass)
    }

    // detects (varargs = arrayOf(...))
    private fun IrVararg.extractSingleArraySpreadOfMatchers(): IrVararg? {
        if (elements.size != 1) return null
        val element = elements[0]
        if (element !is IrSpreadElement) return null
        val call = element.expression
        if (call !is IrCall) return null
        val vararg = call.arguments.singleOrNull { it is IrVararg && it.varargElementType.isMatcher() }
        if (vararg == null) return null
        val func = call.symbol.owner
        val funcName = func.name.asString()
        if (func.kotlinFqName.parent() != BUILT_INS_PACKAGE_FQ_NAME) return null
        if (funcName != "arrayOf" && !funcName.endsWith("ArrayOf")) return null
        return vararg as IrVararg
    }

    private fun IrBlockBodyBuilder.irCallDefaultValuesMatcherContractorFor(
        call: IrCall
    ) = irCallConstructor(defaultValuesMatcherConstructor) {
        arguments[0] = irLong(call.calculateDefaultsMask())
        arguments[1] = createDefaultsExtractingLambdaFor(call)
        arguments[2] = irBoolean(call.symbol.owner.isSuspend)
    }

    private fun IrCall.calculateDefaultsMask(): Long {
        var mask = 0L
        for (index in 1..<arguments.size) {
            if (arguments[index] == null) mask = mask or (1L shl (index - 1))
        }
        return mask
    }

    private fun IrBuilderWithScope.createDefaultsExtractingLambdaFor(call: IrCall): IrFunctionExpression {
        val calledFunc = call.symbol.owner
        val lambdaClass = if (calledFunc.isSuspend) {
            irBuiltIns.suspendFunctionN(2)
        } else {
            irBuiltIns.functionN(2)
        }
        val lambdaType = lambdaClass.typeWith(
            irBuiltIns.anyNType,
            irBuiltIns.listClass.typeWith(irBuiltIns.anyNType),
            irBuiltIns.nothingType
        )
        return irLambdaOf(lambdaType) { func ->
            val substitutionMap = call.typeSubstitutionMap
            +irCall(calledFunc, calledFunc.returnType.substitute(substitutionMap)) {
                call.typeArguments.forEachIndexed { i, it -> typeArguments[i] = it }
                arguments[0] = irGet(func.parameters[0])
                for (index in 1..<call.arguments.size) {
                    val list = irGet(func.parameters[1])
                    val params = call.symbol.owner.parameters
                    val arg = call.arguments[index]
                    if (arg == null) {
                        arguments[index] = null
                    } else {
                        arguments[index] = irAs(
                            argument = irCallListGet(list, index - 1),
                            type = params[index].type.substitute(substitutionMap)
                        )
                    }
                }
            }
        }
    }
}
