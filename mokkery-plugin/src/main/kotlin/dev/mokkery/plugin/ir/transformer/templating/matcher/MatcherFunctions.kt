package dev.mokkery.plugin.ir.transformer.templating.matcher

import dev.mokkery.plugin.core.ir.transformer.TransformerScope
import dev.mokkery.plugin.core.ir.transformer.referenced
import dev.mokkery.plugin.core.ir.transformer.referencedDefaultType
import dev.mokkery.plugin.core.ir.transformer.referencedSymbol
import dev.mokkery.plugin.ir.MokkeryIr
import org.jetbrains.kotlin.ir.declarations.IrFunction
import org.jetbrains.kotlin.ir.declarations.IrValueParameter
import org.jetbrains.kotlin.ir.util.hasAnnotation
import org.jetbrains.kotlin.name.SpecialNames

enum class MatcherFunctionKind(val isIntrinsic: Boolean) {
    Builder(false),
    Matches(true),
    SpreadArray(true),
    Materializer(true),
    MaterializerVararg(true),
}

context(scope: TransformerScope)
fun IrFunction.matcherFunctionKind(): MatcherFunctionKind? = when {
    name == SpecialNames.ANONYMOUS || matcherScopeParameterOrNull() == null -> null
    this == referenced(MokkeryIr.Function.matches) -> MatcherFunctionKind.Matches
    this == referenced(MokkeryIr.Function.materialize) -> MatcherFunctionKind.Materializer
    this == referenced(MokkeryIr.Function.materializeVararg) -> MatcherFunctionKind.MaterializerVararg
    this == referenced(MokkeryIr.Function.spreadArray) -> MatcherFunctionKind.SpreadArray
    else -> MatcherFunctionKind.Builder
}

context(scope: TransformerScope)
fun IrFunction.matcherScopeParameter(): IrValueParameter = matcherScopeParameterOrNull()
    ?: error("MokkeryMatcherScope parameter not found!")

context(scope: TransformerScope)
fun IrFunction.matcherScopeParameterOrNull(): IrValueParameter? {
    val matcherScopeType = referencedDefaultType(MokkeryIr.Class.MokkeryMatcherScope)
    return parameters.firstOrNull { it.type == matcherScopeType }
}

context(scope: TransformerScope)
fun IrValueParameter.isMatcherParameter(): Boolean = hasAnnotation(referencedSymbol(MokkeryIr.Class.Matcher))
