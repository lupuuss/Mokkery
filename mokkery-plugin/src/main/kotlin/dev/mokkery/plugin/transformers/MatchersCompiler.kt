package dev.mokkery.plugin.transformers

import dev.mokkery.plugin.core.CompilerPluginScope
import dev.mokkery.plugin.core.CoreTransformer
import dev.mokkery.plugin.core.Mokkery
import dev.mokkery.plugin.core.getClass
import dev.mokkery.plugin.core.getFunction
import org.jetbrains.kotlin.ir.IrStatement
import org.jetbrains.kotlin.ir.declarations.IrDeclarationOrigin
import org.jetbrains.kotlin.ir.declarations.IrFunction
import org.jetbrains.kotlin.ir.declarations.IrSimpleFunction
import org.jetbrains.kotlin.ir.irAttribute
import org.jetbrains.kotlin.ir.symbols.IrValueParameterSymbol
import org.jetbrains.kotlin.ir.types.typeWith
import org.jetbrains.kotlin.ir.util.defaultType
import org.jetbrains.kotlin.ir.util.getArrayElementType
import org.jetbrains.kotlin.ir.util.hasAnnotation
import org.jetbrains.kotlin.ir.util.isVararg
import org.jetbrains.kotlin.ir.util.nestedClasses


var IrFunction.isCompiledMatcher: Boolean? by irAttribute(copyByDefault = true)

class MatchersCompiler(
    compilerPluginScope: CompilerPluginScope
) : CoreTransformer(compilerPluginScope) {

    private val argMatcherClass = getClass(Mokkery.Class.ArgMatcher)
    private val argMatcherCompositeClass = argMatcherClass.nestedClasses.single { it.name.asString() == "Composite" }
    private val varargMatcherClass = getClass(Mokkery.Class.VarArgMatcher)

    private val matcherAnnotation = getClass(Mokkery.Class.Matcher).symbol
    private val varargMatcherBuilderAnnotation = getClass(Mokkery.Class.VarArgMatcherBuilder).symbol
    private val matcherScopeType = getClass(Mokkery.Class.MokkeryMatcherScope).defaultType
    private val coreMatchesFunctions = setOf(
        getFunction(Mokkery.Function.matches),
        getFunction(Mokkery.Function.matchesComposite)
    )

    fun compileIfMatcher(function: IrSimpleFunction): IrSimpleFunction {
        if (function.isCompiledMatcher != null) return function
        if (function.parameters.none { it.type == matcherScopeType }) {
            function.isCompiledMatcher = false
            return function
        }
        return when {
            function in coreMatchesFunctions -> function.apply { isCompiledMatcher = true }
            else -> function.apply {
                isCompiledMatcher = true
                transformSignature()
                transformBody()
            }
        }
    }

    override fun visitFunctionNew(declaration: IrFunction): IrStatement {
        if (declaration !is IrSimpleFunction) return declaration
        if (declaration.origin != IrDeclarationOrigin.DEFINED) return declaration
        return compileIfMatcher(declaration)
    }

    private fun IrFunction.transformBody() {
        body = body?.let { body ->
            val inliner = MatchersInliner(
                compilerPluginScope = this@MatchersCompiler,
                compileIfMatcher = this@MatchersCompiler::compileIfMatcher,
                initialValueDeclarations = parameters.filter { it.hasAnnotation(matcherAnnotation) }
            )
            inliner.withScope(currentScopeValue) { body.transform(inliner, null) }
        }
    }

    private fun IrFunction.transformSignature(): List<IrValueParameterSymbol> {
        val matcherParams = transformCompositeParamsTypes()
        val type = when {
            matcherParams.any() -> argMatcherCompositeClass.typeWith(returnType)
            hasAnnotation(varargMatcherBuilderAnnotation) -> varargMatcherClass.defaultType
            else -> argMatcherClass.typeWith(returnType)
        }
        returnType = type
        return matcherParams
    }

    private fun IrFunction.transformCompositeParamsTypes(): List<IrValueParameterSymbol> {
        val matcherParams = mutableListOf<IrValueParameterSymbol>()
        parameters.forEach {
            if (it.hasAnnotation(matcherAnnotation)) {
                matcherParams += it.symbol
                if (it.isVararg) {
                    val irBuiltIns = pluginContext.irBuiltIns
                    val matcherType = argMatcherClass.typeWith(it.type.getArrayElementType(irBuiltIns))
                    it.type = irBuiltIns.arrayClass.typeWith(matcherType)
                    it.varargElementType = matcherType
                } else {
                    it.type = argMatcherClass.typeWith(it.type)
                }
            }
        }
        return matcherParams
    }
}
