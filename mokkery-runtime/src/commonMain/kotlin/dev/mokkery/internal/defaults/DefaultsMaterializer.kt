package dev.mokkery.internal.defaults

import dev.mokkery.MokkeryInstanceScope
import dev.mokkery.internal.MokkeryCollection
import dev.mokkery.internal.context.functions
import dev.mokkery.internal.contracts.contracts
import dev.mokkery.internal.contracts.defaults
import dev.mokkery.internal.getScope
import dev.mokkery.internal.instanceIdString
import dev.mokkery.internal.matcher.CallEntry
import dev.mokkery.internal.matcher.DefaultValuesMatcher
import dev.mokkery.internal.matcher.MaterializedDefaultValueMatcher
import dev.mokkery.internal.mokkeryRuntimeError
import dev.mokkery.internal.rendering.callTemplateRenderer
import dev.mokkery.internal.rendering.withRenderingScope
import dev.mokkery.rendering.descriptionRenderer
import dev.mokkery.internal.templating.CallTemplate
import dev.mokkery.internal.utils.runSuspensionNothing
import dev.mokkery.internal.utils.unsafeCast

internal interface DefaultsMaterializer {

    fun materialize(template: CallTemplate, entry: CallEntry): CallTemplate

    fun checkNonDeterministicDefaults(template: CallTemplate, entry: CallEntry, materialized: CallTemplate)

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

    override fun materialize(template: CallTemplate, entry: CallEntry): CallTemplate {
        // we need only first DefaultValueMatcher - the same instance is passed on each default
        val defaultsMatcher = template.firstDefaultValuesMatcherOrNull() ?: return template
        val scope = collection.getScope(template.instanceId)
        val defaults = scope.extractDefaults(defaultsMatcher, entry.args, template)
        var defaultsCount = 0
        val materializedMatchers = template.matchers.mapIndexed { parameterIndex, matcher ->
            when (matcher) {
                is DefaultValuesMatcher -> {
                    MaterializedDefaultValueMatcher(defaults.defaultAt(defaultsCount++, parameterIndex, scope, template))
                }
                else -> matcher
            }
        }
        return template.copy(matchers = materializedMatchers)
    }

    override fun checkNonDeterministicDefaults(template: CallTemplate, entry: CallEntry, materialized: CallTemplate) {
        val repeated = materialize(template, entry)
        if (repeated.matchers == materialized.matchers) return
        materialized.matchers.forEachIndexed { parameterIndex, matcher ->
            if (matcher !is MaterializedDefaultValueMatcher) return@forEachIndexed
            val other = repeated.matchers[parameterIndex]
            if (other !is MaterializedDefaultValueMatcher) return@forEachIndexed
            if (matcher.defaultValue == other.defaultValue) return@forEachIndexed
            collection
                .getScope(template.instanceId)
                .unmatchableDefaultValueError(template, parameterIndex, matcher.defaultValue, other.defaultValue)
        }
    }
}

private fun MokkeryInstanceScope.unmatchableDefaultValueError(
    template: CallTemplate,
    parameterIndex: Int,
    first: Any?,
    second: Any?,
): Nothing = withRenderingScope {
    val function = functions[template.functionId]
    mokkeryRuntimeError(
        "Call template `${callTemplateRenderer.render(template)}` relies on the default value of" +
                " `${function.parameters.getOrNull(parameterIndex)?.name}` in `${function.name}`," +
                " but Mokkery cannot match on it -" +
                " evaluating that default twice produced values that are not equal" +
                " (${descriptionRenderer.render(first)} and ${descriptionRenderer.render(second)})." +
                " Either the default is not deterministic (random values, current time, counters, etc.)," +
                " or its value does not implement structural equality." +
                " Pass that argument explicitly in the `every`/`verify` block that registered this template."
    )
}

private fun List<Any?>.defaultAt(
    index: Int,
    parameterIndex: Int,
    scope: MokkeryInstanceScope,
    template: CallTemplate,
): Any? = getOrElse(index) {
    val function = scope.functions[template.functionId]
    mokkeryRuntimeError(
        "Failed to materialize the default value of `${function.parameters.getOrNull(parameterIndex)?.name}`" +
                " in `${function.name}`!" +
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
): Nothing = withRenderingScope {
    val parameters = functions[template.functionId].parameters
    val omitted = template
        .matchers
        .withIndex()
        .filter { it.value is DefaultValuesMatcher }
        .joinToString { "`${parameters[it.index].name}`" }
    mokkeryRuntimeError(
        "Call template `${callTemplateRenderer.render(template)}` relies on the default value of $omitted," +
                " but one of those defaults is computed from `$usedMember` of the same mocked instance," +
                " which Mokkery cannot resolve." +
                " Pass that argument explicitly in the `every`/`verify` block that registered this template."
    )
}

private fun MokkeryInstanceScope.createDefaultsExtractor(template: CallTemplate): Any {
    val contract = contracts.defaults ?: mokkeryRuntimeError("Default arguments are not supported by $instanceIdString!")
    return contract.mokkeryCreateExtractor(template.functionId.value)
}

private fun CallTemplate.firstDefaultValuesMatcherOrNull() = matchers
    .firstNotNullOfOrNull { it as? DefaultValuesMatcher }
