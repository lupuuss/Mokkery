package dev.mokkery.plugin.ir.transformer.templating

import dev.mokkery.plugin.core.ir.transformer.TransformerScope
import dev.mokkery.plugin.ir.transformer.templating.matcher.matcherFunctionKind
import dev.mokkery.plugin.ir.transformer.templating.matcher.matcherScopeParameter
import dev.mokkery.plugin.ir.transformer.templating.signature.matcherBuilderSignatures
import dev.mokkery.plugin.ir.transformer.templating.phases.ContextFunctionsInliningTransformer
import dev.mokkery.plugin.ir.transformer.templating.phases.MatcherIntrinsicsLoweringTransformer
import dev.mokkery.plugin.ir.transformer.templating.phases.MatcherNormalizingTransformer
import dev.mokkery.plugin.ir.transformer.templating.phases.MatcherRetypingTransformer
import dev.mokkery.plugin.ir.transformer.templating.phases.TemplatingCleanupTransformer
import dev.mokkery.plugin.ir.transformer.templating.phases.TemplatingTransformer
import org.jetbrains.kotlin.ir.declarations.IrFunction
import org.jetbrains.kotlin.ir.declarations.IrSimpleFunction

/**
 * Compiles the body of [function] if it is a matcher builder. Its signature is retyped at the end of the module.
 *
 * Phases: [MatcherNormalizingTransformer], [MatcherRetypingTransformer], [MatcherIntrinsicsLoweringTransformer].
 */
context(scope: TransformerScope)
fun runMatcherPhases(function: IrSimpleFunction) {
    val kind = function.matcherFunctionKind() ?: return
    if (kind.isIntrinsic) return
    matcherBuilderSignatures.register(function)
    function.transform(MatcherNormalizingTransformer(scope, function.matcherScopeParameter(), function), null)
    function.transform(MatcherRetypingTransformer(scope), null)
    function.transform(MatcherIntrinsicsLoweringTransformer(scope), null)
}

/**
 * Turns the `every`/`verify` block [function] into a templating block.
 *
 * Phases: [ContextFunctionsInliningTransformer], [TemplatingTransformer], [MatcherNormalizingTransformer],
 * [MatcherRetypingTransformer], [MatcherIntrinsicsLoweringTransformer], [TemplatingCleanupTransformer].
 */
context(scope: TransformerScope)
fun runTemplatingPhases(function: IrSimpleFunction): IrFunction = function
    .transform(ContextFunctionsInliningTransformer(scope), null)
    .transform(TemplatingTransformer(scope, function.parameters[0]), null)
    .transform(MatcherNormalizingTransformer(scope, function.parameters[0], matcherFunction = null), null)
    .transform(MatcherRetypingTransformer(scope), null)
    .transform(MatcherIntrinsicsLoweringTransformer(scope), null)
    .transform(TemplatingCleanupTransformer(scope, function.symbol), null) as IrFunction
