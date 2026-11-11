package dev.mokkery.plugin.ir.transformer.templating

import dev.mokkery.plugin.core.ir.transformer.TransformerScope
import dev.mokkery.plugin.ir.transformer.templating.matcher.matcherFunctionKind
import dev.mokkery.plugin.ir.transformer.templating.matcher.matcherScopeParameter
import dev.mokkery.plugin.ir.transformer.templating.phases.ContextFunctionsInliningTransformer
import dev.mokkery.plugin.ir.transformer.templating.phases.MatcherIntrinsicsLoweringTransformer
import dev.mokkery.plugin.ir.transformer.templating.phases.MatcherNormalizingTransformer
import dev.mokkery.plugin.ir.transformer.templating.phases.MatcherRetypingTransformer
import dev.mokkery.plugin.ir.transformer.templating.phases.TemplatingCleanupTransformer
import dev.mokkery.plugin.ir.transformer.templating.phases.TemplatingTransformer
import org.jetbrains.kotlin.ir.declarations.IrFunction
import org.jetbrains.kotlin.ir.declarations.IrSimpleFunction
import org.jetbrains.kotlin.ir.declarations.IrValueParameter
import org.jetbrains.kotlin.ir.irAttribute

var IrFunction.isCompiledMatcher: Boolean? by irAttribute(copyByDefault = true)

/**
 * Compiles [function] if it is a matcher builder and returns it. Runs at most once per function.
 *
 * Phases: [MatcherNormalizingTransformer], [MatcherRetypingTransformer], [MatcherIntrinsicsLoweringTransformer].
 */
context(scope: TransformerScope)
fun runMatcherPhases(function: IrSimpleFunction): IrSimpleFunction {
    if (function.isCompiledMatcher != null) return function
    val kind = function.matcherFunctionKind()
    function.isCompiledMatcher = kind != null
    if (kind == null || kind.isIntrinsic) return function
    function.transform(MatcherNormalizingTransformer(scope, function.matcherScopeParameter()!!, function), null)
    function.transform(MatcherRetypingTransformer(scope, function), null)
    function.transform(MatcherIntrinsicsLoweringTransformer(scope), null)
    return function
}

/**
 * Turns the `every`/`verify` block [function] into a templating block that uses [templatingScopeParam].
 *
 * Phases: [ContextFunctionsInliningTransformer], [MatcherNormalizingTransformer], [MatcherRetypingTransformer],
 * [MatcherIntrinsicsLoweringTransformer], [TemplatingTransformer], [TemplatingCleanupTransformer].
 */
context(scope: TransformerScope)
fun runTemplatingPhases(function: IrSimpleFunction, templatingScopeParam: IrValueParameter): IrFunction = function
    .transform(ContextFunctionsInliningTransformer(scope), null)
    .transform(MatcherNormalizingTransformer(scope, function.parameters[0], matcherFunction = null), null)
    .transform(MatcherRetypingTransformer(scope, matcherFunction = null), null)
    .transform(MatcherIntrinsicsLoweringTransformer(scope), null)
    .transform(TemplatingTransformer(scope, templatingScopeParam), null)
    .transform(TemplatingCleanupTransformer(scope, function.symbol), null) as IrFunction
