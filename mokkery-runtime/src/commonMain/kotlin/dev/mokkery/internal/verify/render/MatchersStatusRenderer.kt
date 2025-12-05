package dev.mokkery.internal.verify.render

import dev.mokkery.internal.render.Renderer
import dev.mokkery.internal.templating.CallTemplate
import dev.mokkery.context.CallArgument
import dev.mokkery.internal.tracing.CallTrace
import dev.mokkery.internal.defaults.DefaultsMaterializer
import dev.mokkery.internal.matcher.DefaultValuesMatcher
import dev.mokkery.matcher.ArgMatcher

internal class MatchersStatusRenderer(
    private val materializer: DefaultsMaterializer,
    private val matcherRenderer: Renderer<ArgMatcher<*>>,
    private val valueRenderer: Renderer<Any?>,
) : Renderer<Pair<CallTemplate, CallTrace>> {

    override fun render(value: Pair<CallTemplate, CallTrace>): String {
        val (template, trace) = value
        val defaultMatchersCount = template.matchers.values.count { it is DefaultValuesMatcher }
        val nonDefaultMatchersCount = template.matchers.size - defaultMatchersCount
        val materializedTemplate = when {
            defaultMatchersCount == 0 -> template
            // we only materialize defaults when other matchers are satisfied
            trace.countNonDefaultMatching(template) == nonDefaultMatchersCount -> materializer.materialize(trace, template)
            else -> template
        }
        return buildString {
            trace.args.forEach {
                append(it.describeMatchingAgainst(materializedTemplate.matchers[it.parameter.name]))
            }
        }
    }

    private fun CallArgument.describeMatchingAgainst(matcher: ArgMatcher<Any?>?): String = buildString {
        val matches = matcher?.matches(value) == true
        val status = when {
            matches -> "[+]"
            matcher is DefaultValuesMatcher -> "[?]"
            else -> "[-]"
        }
        val statusLine = "$status ${parameter.name}:"
        val matcherRendered = matcher?.let(matcherRenderer::render) ?: "null"
        append(statusLine)
        when {
            matches -> appendLine(" $matcherRendered ~ ${valueRenderer.render(value)}")
            matcher is DefaultValuesMatcher -> {
                appendLine()
                appendLine("   expect: default() => Cannot be determined, because other matchers don't match!")
                appendLine("   actual: ${valueRenderer.render(value)}")
            }
            else -> {
                appendLine()
                appendLine("   expect: $matcherRendered")
                appendLine("   actual: ${valueRenderer.render(value)}")
            }
        }
    }

    private fun CallTrace.countNonDefaultMatching(template: CallTemplate): Int = args.count { arg ->
        val matcher = template.matchers[arg.parameter.name]
        if (matcher is DefaultValuesMatcher) return@count false
        matcher?.matches(arg.value) == true
    }
}
