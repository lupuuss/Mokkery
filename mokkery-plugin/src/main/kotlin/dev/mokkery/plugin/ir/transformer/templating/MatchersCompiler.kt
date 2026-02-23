package dev.mokkery.plugin.ir.transformer.templating

import dev.mokkery.plugin.ir.MokkeryIr
import dev.mokkery.plugin.ir.irBuiltIns
import dev.mokkery.plugin.ir.transformer.core.TransformerScope
import dev.mokkery.plugin.ir.transformer.core.referenced
import dev.mokkery.plugin.ir.transformer.core.referencedDefaultType
import dev.mokkery.plugin.ir.transformer.core.referencedSymbol
import org.jetbrains.kotlin.ir.declarations.IrFunction
import org.jetbrains.kotlin.ir.declarations.IrSimpleFunction
import org.jetbrains.kotlin.ir.irAttribute
import org.jetbrains.kotlin.ir.symbols.IrValueParameterSymbol
import org.jetbrains.kotlin.ir.types.typeWith
import org.jetbrains.kotlin.ir.util.getArrayElementType
import org.jetbrains.kotlin.ir.util.hasAnnotation
import org.jetbrains.kotlin.ir.util.isVararg

var IrFunction.isCompiledMatcher: Boolean? by irAttribute(copyByDefault = true)

class MatchersCompiler(scope: TransformerScope): TransformerScope by scope {

    private val argMatcherClass = referenced(MokkeryIr.Class.ArgMatcher)
    private val argMatcherCompositeClass = referenced(MokkeryIr.Class.ArgMatcherComposite)

    private val matcherAnnotationSymbol = referencedSymbol(MokkeryIr.Class.Matcher)
    private val matcherScopeType = referencedDefaultType(MokkeryIr.Class.MokkeryMatcherScope)
    private val intrinsicsMatchesFunctions = setOf(
        referenced(MokkeryIr.Function.matches),
        referenced(MokkeryIr.Function.matchesComposite)
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
                pluginScope = this@MatchersCompiler,
                matchersCompiler = this@MatchersCompiler,
                initialValueDeclarations = parameters.filter { it.hasAnnotation(matcherAnnotationSymbol) }
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
            if (it.hasAnnotation(matcherAnnotationSymbol)) {
                matcherParams += it.symbol
                if (it.isVararg) {
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
