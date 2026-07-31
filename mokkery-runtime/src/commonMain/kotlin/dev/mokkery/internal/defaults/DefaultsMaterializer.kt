package dev.mokkery.internal.defaults

import dev.mokkery.MokkeryInstanceScope
import dev.mokkery.internal.MokkeryCollection
import dev.mokkery.internal.getScope
import dev.mokkery.internal.matcher.DefaultValuesMatcher
import dev.mokkery.internal.matcher.MaterializedDefaultValueMatcher
import dev.mokkery.internal.mokkeryRuntimeError
import dev.mokkery.internal.templating.CallTemplate
import dev.mokkery.internal.tracing.CallTrace
import dev.mokkery.internal.utils.runSuspensionNothing
import dev.mokkery.internal.utils.unsafeCast

internal interface DefaultsMaterializer {

    fun materialize(trace: CallTrace, template: CallTemplate): CallTemplate

    fun interface  Factory {

        fun create(instances: MokkeryCollection): DefaultsMaterializer

        companion object {

            fun default() = Factory(::DefaultsMaterializer)
        }
    }
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
            .mapValues { (name, matcher) ->
                matcher
                    .takeIf { it !is DefaultValuesMatcher }
                    ?: MaterializedDefaultValueMatcher(defaults.defaultAt(defaultsCount++, template, name))
            }
        return template.copy(matchers = materializedMatchers)
    }
}

private fun List<Any?>.defaultAt(index: Int, template: CallTemplate, parameter: String): Any? = getOrElse(index) {
    mokkeryRuntimeError(
        "Failed to materialize the default value of `$parameter` in `${template.name}`!" +
                " Expected at least ${index + 1} extracted default(s), but got $size." +
                " It's an internal Mokkery error, please report it."
    )
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
