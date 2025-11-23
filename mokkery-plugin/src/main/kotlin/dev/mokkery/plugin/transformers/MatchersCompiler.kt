package dev.mokkery.plugin.transformers

import dev.mokkery.plugin.core.Mokkery
import dev.mokkery.plugin.core.TransformerScope
import dev.mokkery.plugin.core.getClass
import dev.mokkery.plugin.core.getFunction
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

class MatchersCompiler(private val transformerScope: TransformerScope) {

    private val argMatcherClass = transformerScope.getClass(Mokkery.Class.ArgMatcher)
    private val argMatcherCompositeClass = argMatcherClass.nestedClasses.single { it.name.asString() == "Composite" }

    private val matcherAnnotation = transformerScope.getClass(Mokkery.Class.Matcher).symbol
    private val matcherScopeType = transformerScope.getClass(Mokkery.Class.MokkeryMatcherScope).defaultType
    private val intrinsicsMatchesFunctions = setOf(
        transformerScope.getFunction(Mokkery.Function.matches),
        transformerScope.getFunction(Mokkery.Function.matchesComposite)
    )

    fun compileIfMatcher(function: IrSimpleFunction): IrSimpleFunction {
        if (function.isCompiledMatcher != null) return function
        if (function.parameters.none { it.type == matcherScopeType }) {
            function.isCompiledMatcher = false
            return function
        }
        return when {
            function in intrinsicsMatchesFunctions -> function.apply { isCompiledMatcher = true }
            else -> function.apply {
                isCompiledMatcher = true
                transformSignature()
                transformBody()
            }
        }
    }

    private fun IrFunction.transformBody() {
        body = body?.let { body ->
            val inliner = MatchersInliningTransformer(
                compilerPluginScope = transformerScope,
                compileIfMatcher = this@MatchersCompiler::compileIfMatcher,
                initialValueDeclarations = parameters.filter { it.hasAnnotation(matcherAnnotation) }
            )
            inliner.withScope(this) {
                body.transform(inliner, null)
            }
        }
    }

    private fun IrFunction.transformSignature(): List<IrValueParameterSymbol> {
        val matcherParams = transformCompositeParamsTypes()
        val type = when {
            matcherParams.any() -> argMatcherCompositeClass.typeWith(returnType)
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
                    val irBuiltIns = transformerScope.pluginContext.irBuiltIns
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
