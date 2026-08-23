package dev.mokkery.plugin.ir.transformer.templating

import dev.mokkery.plugin.core.ir.irBuiltIns
import dev.mokkery.plugin.core.ir.transformer.TransformerScope
import dev.mokkery.plugin.core.ir.transformer.referenced
import dev.mokkery.plugin.core.ir.transformer.referencedDefaultType
import dev.mokkery.plugin.core.ir.transformer.referencedSymbol
import dev.mokkery.plugin.ir.MokkeryIr
import org.jetbrains.kotlin.ir.declarations.IrFunction
import org.jetbrains.kotlin.ir.declarations.IrSimpleFunction
import org.jetbrains.kotlin.ir.irAttribute
import org.jetbrains.kotlin.ir.symbols.IrValueParameterSymbol
import org.jetbrains.kotlin.ir.types.typeWith
import org.jetbrains.kotlin.ir.util.getArrayElementType
import org.jetbrains.kotlin.ir.util.hasAnnotation
import org.jetbrains.kotlin.ir.util.isVararg

var IrFunction.isCompiledMatcher: Boolean? by irAttribute(copyByDefault = true)

context(scope: TransformerScope)
fun compileIfMatcher(function: IrSimpleFunction): IrSimpleFunction {
    if (function.isCompiledMatcher != null) return function
    val matcherScopeType = referencedDefaultType(MokkeryIr.Class.MokkeryMatcherScope)
    if (function.parameters.none { it.type == matcherScopeType }) {
        function.isCompiledMatcher = false
        return function
    }
    val matchesIntrinsics = setOf(
        referenced(MokkeryIr.Function.matches),
        referenced(MokkeryIr.Function.matchesComposite),
    )
    return when {
        function in matchesIntrinsics -> function.apply { isCompiledMatcher = true }
        else -> function.apply {
            isCompiledMatcher = true
            transformSignature()
            transformBody()
        }
    }
}

context(scope: TransformerScope)
private fun IrFunction.transformBody() {
    val matcherAnnotationSymbol = referencedSymbol(MokkeryIr.Class.Matcher)
    body = body?.let { body ->
        val inliner = MatchersInliningTransformer(
            pluginScope = scope,
            initialValueDeclarations = parameters.filter { it.hasAnnotation(matcherAnnotationSymbol) }
        )
        inliner.withScope(this) {
            body.transform(inliner, null)
        }
    }
}

context(scope: TransformerScope)
private fun IrFunction.transformSignature(): List<IrValueParameterSymbol> {
    val argMatcherClass = referenced(MokkeryIr.Class.ArgMatcher)
    val argMatcherCompositeClass = referenced(MokkeryIr.Class.ArgMatcherComposite)
    val matcherParams = transformCompositeParamsTypes()
    val type = when {
        matcherParams.any() -> argMatcherCompositeClass.typeWith(returnType)
        else -> argMatcherClass.typeWith(returnType)
    }
    returnType = type
    return matcherParams
}

context(scope: TransformerScope)
private fun IrFunction.transformCompositeParamsTypes(): List<IrValueParameterSymbol> {
    val argMatcherClass = referenced(MokkeryIr.Class.ArgMatcher)
    val matcherParams = mutableListOf<IrValueParameterSymbol>()
    val matcherAnnotationSymbol = referencedSymbol(MokkeryIr.Class.Matcher)
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
