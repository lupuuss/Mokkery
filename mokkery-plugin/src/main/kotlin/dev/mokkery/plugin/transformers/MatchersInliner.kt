package dev.mokkery.plugin.transformers

import dev.mokkery.plugin.core.CompilerPluginScope
import dev.mokkery.plugin.core.CoreTransformer
import dev.mokkery.plugin.core.Mokkery
import dev.mokkery.plugin.core.declarationIrBuilder
import dev.mokkery.plugin.core.getClass
import dev.mokkery.plugin.core.getFunction
import dev.mokkery.plugin.core.mokkeryErrorAt
import dev.mokkery.plugin.ir.irCall
import dev.mokkery.plugin.ir.irCallConstructor
import dev.mokkery.plugin.ir.irCallListOf
import dev.mokkery.plugin.ir.irInvoke
import dev.mokkery.plugin.ir.irVararg
import org.jetbrains.kotlin.ir.IrStatement
import org.jetbrains.kotlin.ir.builders.IrBuilderWithScope
import org.jetbrains.kotlin.ir.builders.createTmpVariable
import org.jetbrains.kotlin.ir.builders.irBlock
import org.jetbrains.kotlin.ir.builders.irGet
import org.jetbrains.kotlin.ir.declarations.IrFunction
import org.jetbrains.kotlin.ir.declarations.IrParameterKind
import org.jetbrains.kotlin.ir.declarations.IrValueParameter
import org.jetbrains.kotlin.ir.declarations.IrVariable
import org.jetbrains.kotlin.ir.expressions.IrCall
import org.jetbrains.kotlin.ir.expressions.IrExpression
import org.jetbrains.kotlin.ir.expressions.IrGetValue
import org.jetbrains.kotlin.ir.expressions.IrReturn
import org.jetbrains.kotlin.ir.expressions.IrSetValue
import org.jetbrains.kotlin.ir.expressions.IrSpreadElement
import org.jetbrains.kotlin.ir.expressions.IrVararg
import org.jetbrains.kotlin.ir.expressions.IrWhen
import org.jetbrains.kotlin.ir.expressions.impl.IrSpreadElementImpl
import org.jetbrains.kotlin.ir.symbols.IrSimpleFunctionSymbol
import org.jetbrains.kotlin.ir.types.IrSimpleType
import org.jetbrains.kotlin.ir.types.IrType
import org.jetbrains.kotlin.ir.types.classOrNull
import org.jetbrains.kotlin.ir.types.isArray
import org.jetbrains.kotlin.ir.types.typeWith
import org.jetbrains.kotlin.ir.util.BodyPrintingStrategy
import org.jetbrains.kotlin.ir.util.KotlinLikeDumpOptions
import org.jetbrains.kotlin.ir.util.defaultType
import org.jetbrains.kotlin.ir.util.dumpKotlinLike
import org.jetbrains.kotlin.ir.util.eraseTypeParameters
import org.jetbrains.kotlin.ir.util.functions
import org.jetbrains.kotlin.ir.util.getArrayElementType
import org.jetbrains.kotlin.ir.util.isSubclassOf
import org.jetbrains.kotlin.ir.util.kotlinFqName
import org.jetbrains.kotlin.ir.util.nestedClasses
import org.jetbrains.kotlin.ir.util.overrides
import org.jetbrains.kotlin.ir.util.primaryConstructor
import org.jetbrains.kotlin.ir.util.transformInPlace
import org.jetbrains.kotlin.name.CallableId

class MatchersInliner(
    compilerPluginScope: CompilerPluginScope,
    private val argMatchersScopeParam: IrValueParameter,
    private val compiledMatchersLookUp: (IrFunction) -> IrSimpleFunctionSymbol?,
) : CoreTransformer(compilerPluginScope) {

    private val argMatcherClass = getClass(Mokkery.Class.ArgMatcher)
    private val varargMatcherClass = getClass(Mokkery.Class.VarArgMatcher)
    private val varargMatcherMarkerClass = getClass(Mokkery.Class.VarargMatcherMarker)
    private val argMatcherCompositeClass = argMatcherClass.nestedClasses.single { it.name.asString() == "Composite" }
    private val eqMatcher = getFunction(Mokkery.Function._eqMokkeryMatcher)
    private val argMatchersScopeClass = getClass(Mokkery.Class.ArgMatchersScope)
    private val argMatchersScopeType = argMatchersScopeClass.defaultType
    private val matchesFunction = argMatchersScopeClass.functions.first { it.name.asString() == "matches" }
    private val matchesCompositeFunction = argMatchersScopeClass.functions.first { it.name.asString() == "matchesComposite" }
    private val matchersVariables = mutableListOf<IrVariable>()
    private val inlineLiteralsAsMatchersFunc = getFunction(Mokkery.Function.inlineLiteralsAsMatchers)

    override fun visitCall(expression: IrCall): IrExpression {
        if (!expression.isFrontMatcherCall) return super.visitCall(expression)
        return declarationIrBuilder(expression) {
            replaceMatcher(expression)
        }
    }

    override fun visitVariable(declaration: IrVariable): IrStatement {
        val irVariable = super.visitVariable(declaration)
        if (irVariable !is IrVariable) return irVariable
        if (irVariable.initializer?.type?.isMatcher() != true) return irVariable
        irVariable.type = irVariable.initializer!!.type
        matchersVariables += irVariable
        return irVariable
    }

    override fun visitVararg(expression: IrVararg): IrExpression {
        val vararg = super.visitVararg(expression)
        if (vararg !is IrVararg) return vararg
        val usesMatchers = vararg.elements.any {
            when (it) {
                is IrSpreadElement -> it.expression.type.isMatcher()
                is IrExpression -> it.type.isMatcher()
                else -> false
            }
        }
        if (!usesMatchers) return vararg
        val elementType = argMatcherClass.typeWith(vararg.varargElementType)
        vararg.varargElementType = elementType
        vararg.type = pluginContext.irBuiltIns.arrayClass.owner.typeWith(elementType)
        vararg.elements.transformInPlace { element ->
            when (element) {
                is IrSpreadElement -> when {
                    element.expression.type.isVarArgMatcher() -> element.expression
                    element.expression.type.isComposite() -> varargMatcherMarker(element.expression)
                    element.expression.type.isMatcher() -> error("Regular matchers cannot be used with spread operator!")
                    else -> spreadLiteralsAsMatchers(element)
                }
                else -> {
                    val elementExpression = element as IrExpression
                    elementExpression
                        .takeIf { it.type.isMatcher() }
                        ?: callEqMatcher(elementExpression)
                }
            }

        }
        return vararg
    }

    override fun visitWhen(expression: IrWhen): IrExpression {
        val irWhen = super.visitWhen(expression)
        if (irWhen !is IrWhen) return irWhen
        if (expression.branches.none { branch -> branch.result.type.isMatcher() }) return irWhen
        val anyVarargMatchers = expression.branches.any { it.result.type.isVarArgMatcher() }
        if (anyVarargMatchers) {
            irWhen.type = varargMatcherClass.defaultType
            irWhen.branches.forEach {
                if (it.result.type.isComposite()) {
                    it.result = varargMatcherMarker(it.result)
                }
            }
        } else {
            irWhen.type = argMatcherClass.typeWith(irWhen.type)
            irWhen.branches.forEach {
                if (!it.result.type.isMatcher()) {
                    it.result = callEqMatcher(it.result)
                }
            }
        }
        return irWhen
    }

    override fun visitGetValue(expression: IrGetValue): IrExpression {
        val irGet = super.visitGetValue(expression)
        if (irGet !is IrGetValue) return irGet
        if (irGet.symbol.owner !in matchersVariables) return irGet
        irGet.type = irGet.symbol.owner.type
        return irGet
    }

    override fun visitSetValue(expression: IrSetValue): IrExpression {
        val irSet = super.visitSetValue(expression)
        if (irSet !is IrSetValue) return irSet
        if (irSet.symbol.owner !in matchersVariables) return irSet
        irSet.type = irSet.symbol.owner.type
        if (!irSet.value.type.isMatcher())  {
            irSet.value = callEqMatcher(irSet.value)
        }
        return irSet
    }

    override fun visitReturn(expression: IrReturn): IrExpression {
        if (expression.type != expression.value.type && expression.value.type.isMatcher()) {
            mokkeryErrorAt(
                expression,
                "Matcher must not be returned! You must pass them to mock methods directly or through variables!"
            )
        }
        return super.visitReturn(expression)
    }

    private fun IrBuilderWithScope.replaceMatcher(expression: IrExpression): IrExpression {
        val call = expression as IrCall
        val matcherFrontFunc = call.symbol.owner
        if (matcherFrontFunc.overrides(matchesFunction)) return expression.arguments[1]!!
        if (matcherFrontFunc.overrides(matchesCompositeFunction)) return replaceMatchesComposite(expression)
        val backendName = MatchersCompiler.matcherBackendName(matcherFrontFunc)
        val backendFuncCandidates = pluginContext.referenceFunctions(CallableId(matcherFrontFunc.parent.kotlinFqName, backendName))
        val backendFunc = backendFuncCandidates
            .singleOrNull()
            ?: backendFuncCandidates.matchTo(matcherFrontFunc.parameters)
            ?: compiledMatchersLookUp(matcherFrontFunc)
            ?: mokkeryErrorAt(expression) {
                val noBody = KotlinLikeDumpOptions(bodyPrintingStrategy = BodyPrintingStrategy.NO_BODIES)
                "Failed to resolve matcher for ${matcherFrontFunc.dumpKotlinLike(noBody)}"
            }
        return irCall(backendFunc) {
            call.typeArguments.forEachIndexed { i, it -> typeArguments[i] = it }
            matcherFrontFunc
                .parameters
                .forEachIndexed { i, it ->
                    val argument = expression.arguments[it]
                    arguments[i] = if (it.kind == IrParameterKind.DispatchReceiver || it.type == argMatchersScopeType) {
                        argument
                    } else {
                        replaceNestedTemplatingArg(argument, it, backendFunc.owner.parameters[i])
                    }
                }
        }
    }

    private fun IrBuilderWithScope.replaceMatchesComposite(expression: IrCall): IrExpression {
        val matchersVararg = expression.arguments[1] as IrVararg
        val builderBlock = expression.arguments[2]!!
        // extracting lambda return type
        val returnType = (builderBlock.type as IrSimpleType).arguments.last() as IrType
        return irBlock(resultType = returnType) {
            val elementType = argMatcherClass.typeWith(matchersVararg.varargElementType)
            val matchersInlined = createTmpVariable(
                irCallListOf(
                    transformerScope = this@MatchersInliner,
                    type = elementType,
                    elements = replaceVarargComposite(matchersVararg, elementType).elements
                )
            )
            +irInvoke(builderBlock, false, irGet(matchersInlined))
        }
    }

    private fun IrBuilderWithScope.replaceNestedTemplatingArg(
        frontArg: IrExpression?,
        frontParam: IrValueParameter,
        backParam: IrValueParameter,
    ): IrExpression? = when {
        frontArg is IrVararg && backParam.isMatchersVararg -> replaceVarargComposite(frontArg, backParam.varargElementType!!)
        !backParam.type.isMatcher() -> frontArg.assertNotMatcher(frontParam)
        frontArg == null -> null
        frontArg.type.isMatcher() -> frontArg
        frontArg.isFrontMatcherCall -> replaceMatcher(frontArg)
        else -> callEqMatcher(frontArg)
    }

    private fun IrBuilderWithScope.replaceVarargComposite(
        frontArg: IrVararg,
        varargElementType: IrType
    ): IrVararg {
        return irVararg(
            varargElementType,
            frontArg.elements.map { element ->
                when (element) {
                    is IrSpreadElement -> when {
                        element.expression.type.isMatchersArray() -> element
                        else -> mokkeryErrorAt(element, "Spread operator is not supported in this context!")
                    }
                    is IrExpression -> when {
                        element.type.isMatcher() -> element
                        element.isFrontMatcherCall -> replaceMatcher(element)
                        else -> callEqMatcher(element)
                    }
                    else -> mokkeryErrorAt(element, "Unsupported vararg element!")
                }
            }
        )
    }

    private fun callEqMatcher(expression: IrExpression) = declarationIrBuilder(eqMatcher.symbol) {
        irCall(eqMatcher) {
            arguments[0] = irGet(argMatchersScopeParam)
            arguments[1] = expression
        }
    }

    private fun spreadLiteralsAsMatchers(spread: IrSpreadElement): IrSpreadElement {
        return declarationIrBuilder(inlineLiteralsAsMatchersFunc.symbol) {
            IrSpreadElementImpl(
                startOffset = startOffset,
                endOffset = endOffset,
                expression = irCall(inlineLiteralsAsMatchersFunc) {
                    arguments[0] = spread.expression
                }
            )
        }
    }

    private fun varargMatcherMarker(expression: IrExpression): IrExpression {
        val constructor = varargMatcherMarkerClass.primaryConstructor!!
        return declarationIrBuilder(constructor.symbol) {
            irCallConstructor(constructor) {
                arguments[0] = expression
            }
        }
    }

    private fun Collection<IrSimpleFunctionSymbol>.matchTo(frontParams: List<IrValueParameter>): IrSimpleFunctionSymbol? {
        return find { func ->
            val result = func.owner.parameters.size == frontParams.size && func.owner.parameters
                .zip(frontParams) { b, f -> b.type.classOrNull == f.type.classOrNull || b.type.eraseTypeParameters() == argMatcherClass.typeWith(f.type).eraseTypeParameters() }
                .all { it }
            result
        }
    }

    private fun IrType.isMatcher(): Boolean {
        val classSymbol = classOrNull ?: return false
        val clazz = classSymbol.owner
        if (clazz == argMatcherClass) return true
        return clazz.isSubclassOf(argMatcherClass)
    }

    private fun IrType.isMatchersArray(): Boolean {
        if (!isArray()) return false
        return this.getArrayElementType(pluginContext.irBuiltIns).isMatcher()
    }

    private fun <T : IrExpression?> T.assertNotMatcher(frontParam: IrValueParameter): T {
        if (this == null) return this
        if (!this.isFrontMatcherCall && !this.type.isMatcher()) return this
        mokkeryErrorAt(this) {
            "Matcher passed to a function that does not accept matchers here!" +
                    " Pass something that isn't a matcher" +
                    " or mark `${frontParam.dumpKotlinLike()}`" +
                    " from `${frontParam.parent.kotlinFqName}` with @Matcher annotation!"
        }
    }

    private fun IrType.isComposite(): Boolean {
        val classSymbol = classOrNull ?: return false
        val clazz = classSymbol.owner
        if (clazz == argMatcherCompositeClass) return true
        return clazz.isSubclassOf(argMatcherCompositeClass)
    }

    private fun IrType.isVarArgMatcher(): Boolean {
        val classSymbol = classOrNull ?: return false
        val clazz = classSymbol.owner
        if (clazz == varargMatcherClass) return true
        return clazz.isSubclassOf(varargMatcherClass)
    }

    private val IrExpression?.isFrontMatcherCall: Boolean
        get() {
            if (this !is IrCall) return false
            val owner = symbol.owner
            return owner.parameters.any { it.type == argMatchersScopeType }
        }



    private val IrValueParameter.isMatchersVararg: Boolean
        get() = varargElementType?.isMatcher() == true
}
