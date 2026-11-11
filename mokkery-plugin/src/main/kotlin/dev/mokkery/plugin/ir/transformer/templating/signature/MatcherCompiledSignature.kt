package dev.mokkery.plugin.ir.transformer.templating.signature

import dev.mokkery.plugin.core.ir.irBuiltIns
import dev.mokkery.plugin.core.ir.transformer.TransformerScope
import dev.mokkery.plugin.ir.transformer.templating.matcher.MatcherFunctionKind
import dev.mokkery.plugin.ir.transformer.templating.matcher.asMatcherType
import dev.mokkery.plugin.ir.transformer.templating.matcher.isMatcherParameter
import dev.mokkery.plugin.ir.transformer.templating.matcher.matcherFunctionKind
import org.jetbrains.kotlin.ir.declarations.IrFunction
import org.jetbrains.kotlin.ir.declarations.IrValueParameter
import org.jetbrains.kotlin.ir.irAttribute
import org.jetbrains.kotlin.ir.types.IrType
import org.jetbrains.kotlin.ir.types.typeWith
import org.jetbrains.kotlin.ir.util.isVararg

/**
 * The compiled signature of matcher builder [this], computed from its source signature on first access.
 * Accessing it registers [this] in [MatcherBuilderSignatures]; until then declarations keep their source signatures.
 */
context(scope: TransformerScope)
val IrFunction.matcherCompiledSignature: CompiledSignature
    get() {
        val old = cachedCompiledSignature
        if (old != null) return old
        val new = computeCompiledSignature()
        cachedCompiledSignature = new
        matcherBuilderSignatures.register(this)
        return new
    }

/**
 * The type of matcher parameter [this] in the compiled signature of its function.
 */
context(scope: TransformerScope)
val IrValueParameter.matcherCompiledType: IrType
    get() = (parent as IrFunction).matcherCompiledSignature.parameters.getValue(this).type

private var IrFunction.cachedCompiledSignature: CompiledSignature? by irAttribute(copyByDefault = false)

context(scope: TransformerScope)
private fun IrFunction.computeCompiledSignature(): CompiledSignature {
    val kind = matcherFunctionKind()
    check(kind == MatcherFunctionKind.Builder) {
        "Expected matcher builder, but got $kind!"
    }
    return CompiledSignature(
        returnType = returnType.asMatcherType(),
        parameters = parameters.filter { it.isMatcherParameter() }.associateWith { it.computeCompiledParameter() },
    )
}

context(scope: TransformerScope)
private fun IrValueParameter.computeCompiledParameter(): CompiledParameter = when {
    isVararg -> {
        val elementType = varargElementType!!.asMatcherType()
        CompiledParameter(irBuiltIns.arrayClass.typeWith(elementType), elementType)
    }
    else -> CompiledParameter(type.asMatcherType(), null)
}
