package dev.mokkery.internal.defaults

import dev.mokkery.MokkeryInstanceScope
import dev.mokkery.internal.MokkeryCollection
import dev.mokkery.internal.getScope
import dev.mokkery.internal.matcher.DefaultValuesMatcher
import dev.mokkery.internal.matcher.MaterializedDefaultValueMatcher
import dev.mokkery.internal.templating.CallTemplate
import dev.mokkery.internal.tracing.CallTrace
import dev.mokkery.internal.utils.runSuspensionNothing
import dev.mokkery.internal.utils.unsafeCast

internal interface DefaultsMaterializer {

    fun materialize(trace: CallTrace, template: CallTemplate): CallTemplate
}

internal fun DefaultsMaterializer(
    collection: MokkeryCollection
): DefaultsMaterializer = DefaultsMaterializerImpl(collection)

private class DefaultsMaterializerImpl(
    private val collection: MokkeryCollection
) : DefaultsMaterializer {

    override fun materialize(trace: CallTrace, template: CallTemplate): CallTemplate {
        // we need only first DefaultValueMatcher - the same instance is passed on each default
        val defaultsMatcher = template.firstDefaultValuesMatcherOrNull() ?: return template
        val scope = collection.getScope(template.instanceId)
        val args = trace.args.map { it.value }
        val defaults = scope.extractDefaults(defaultsMatcher, args)
        var defaultsCount = 0
        val materializedMatchers = template
            .matchers
            .mapValues { (_, matcher) ->
                matcher
                    .takeIf { it !is DefaultValuesMatcher }
                    ?: MaterializedDefaultValueMatcher(defaults[defaultsCount++])
            }
        return template.copy(matchers = materializedMatchers)
    }
}

private fun MokkeryInstanceScope.extractDefaults(
    defaultsMatcher: DefaultValuesMatcher,
    args: List<Any?>
): List<Any?> {
    val extractor = defaultsExtractorFactory.createDefaultsExtractor()
    try {
        val extractingFunction = defaultsMatcher.extractingFunction
        when {
            defaultsMatcher.isExtractingFunctionSuspend -> runSuspensionNothing {
                extractingFunction.unsafeCast<suspend (Any, List<Any?>) -> Nothing>().invoke(extractor, args)
            }
            else -> extractingFunction.unsafeCast<(Any, List<Any?>) -> Nothing>().invoke(extractor, args)
        }
    } catch (e: ArgumentsExtractedException) {
        val mask = defaultsMatcher.mask
        return e.values.filterIndexed { i, _ ->
            (mask shr i) and 1L == 1L
        }
    }
}

private fun CallTemplate.firstDefaultValuesMatcherOrNull() = matchers
    .values
    .firstNotNullOfOrNull { it as? DefaultValuesMatcher }
