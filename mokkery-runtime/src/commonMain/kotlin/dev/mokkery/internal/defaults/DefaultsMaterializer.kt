package dev.mokkery.internal.defaults

import dev.mokkery.MokkeryInstanceScope
import dev.mokkery.internal.MokkeryCollection
import dev.mokkery.internal.context.instanceSpec
import dev.mokkery.internal.contracts.defaultsContract
import dev.mokkery.internal.getScope
import dev.mokkery.internal.instanceIdString
import dev.mokkery.internal.matcher.DefaultValuesMatcher
import dev.mokkery.internal.matcher.MaterializedDefaultValueMatcher
import dev.mokkery.internal.mokkeryRuntimeError
import dev.mokkery.internal.rendering.callTemplateRenderer
import dev.mokkery.internal.rendering.withRenderingScope
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
        val defaults = scope.extractDefaults(defaultsMatcher, args, template)
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
    args: List<Any?>,
    template: CallTemplate,
): List<Any?> {
    val extractor = createDefaultsExtractor(template)
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
    } catch (e: UnsupportedDefaultValueException) {
        unsupportedDefaultValueError(template, e.usedMember)
    }
}

private fun MokkeryInstanceScope.unsupportedDefaultValueError(
    template: CallTemplate,
    usedMember: String,
): Nothing = withRenderingScope(instances = instanceSpec.collection) {
    val omitted = template
        .matchers
        .filterValues { it is DefaultValuesMatcher }
        .keys
        .joinToString { "`$it`" }
    mokkeryRuntimeError(
        "Call template `${callTemplateRenderer.render(template)}` relies on the default value of $omitted," +
                " but one of those defaults is computed from `$usedMember` of the same mocked instance," +
                " which Mokkery cannot resolve." +
                " Pass that argument explicitly in the `every`/`verify` block that registered this template."
    )
}

private fun MokkeryInstanceScope.createDefaultsExtractor(template: CallTemplate): Any {
    val contract = defaultsContract ?: mokkeryRuntimeError("Default arguments are not supported by $instanceIdString!")
    return contract.mokkeryCreateExtractor(template.name, template.parameters)
}

private fun CallTemplate.firstDefaultValuesMatcherOrNull() = matchers
    .values
    .firstNotNullOfOrNull { it as? DefaultValuesMatcher }
