package dev.mokkery.internal.defaults

import dev.mokkery.internal.MokkeryCollection
import dev.mokkery.internal.getScope
import dev.mokkery.internal.matcher.DefaultValueMatcher
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

    override fun materialize(
        trace: CallTrace,
        template: CallTemplate
    ): CallTemplate {
        val defaultsExpected = template.matchers.values.filterIsInstance<DefaultValueMatcher<Any?>>()
        return when {
            defaultsExpected.isEmpty() -> template
            else -> {
                // all DefaultValueMatcher instances should have the same properties for single call, so we need only one
                val default = defaultsExpected.first()
                val scope = collection.getScope(template.instanceId)
                val extractor = scope.defaultsExtractorFactory.createDefaultsExtractor()
                val defaults = extractDefaults(
                    default = default,
                    extractor = extractor,
                    args = trace.args.map { it.value }
                )
                var defaultsCount = 0
                val materializedMatchers = template.matchers.mapValues {
                    if (it.value is DefaultValueMatcher<*>) {
                        MaterializedDefaultValueMatcher(defaults[defaultsCount++])
                    } else {
                        it.value
                    }
                }
                template.copy(matchers = materializedMatchers)
            }
        }
    }

    private fun extractDefaults(
        default: DefaultValueMatcher<Any?>,
        extractor: Any,
        args: List<Any?>
    ): List<Any?> {
        try {
            val call = default.caller
            when {
                default.isSuspend -> runSuspensionNothing {
                    call.unsafeCast<suspend (Any, List<Any?>) -> Nothing>().invoke(extractor, args)
                }
                else -> call.unsafeCast<(Any, List<Any?>) -> Nothing>().invoke(extractor, args)
            }
        } catch (e: ArgumentsExtractedException) {
            val mask = default.mask
            return e.values.filterIndexed { i, _ ->
                (mask shr i) and 1L == 1L
            }
        }
    }
}
