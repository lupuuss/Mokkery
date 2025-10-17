package dev.mokkery.internal.verify.render

import dev.mokkery.internal.render.Renderer
import dev.mokkery.internal.render.ToStringRenderer
import dev.mokkery.internal.render.ValueDescriptionRenderer
import dev.mokkery.internal.templating.CallTemplate
import dev.mokkery.context.CallArgument
import dev.mokkery.internal.tracing.CallTrace
import dev.mokkery.internal.defaults.DefaultsMaterializer
import dev.mokkery.internal.matcher.DefaultValueMatcher
import dev.mokkery.matcher.ArgMatcher

internal class MatchersStatusRenderer(
    private val materializer: DefaultsMaterializer,
    private val matcherRenderer: Renderer<ArgMatcher<*>> = ToStringRenderer,
    private val valueRenderer: Renderer<Any?> = ValueDescriptionRenderer
) : Renderer<Pair<CallTemplate, CallTrace>> {

    override fun render(value: Pair<CallTemplate, CallTrace>): String {
        val (template, trace) = value
        val defaultsCount = template.matchers.values.count { it is DefaultValueMatcher<*> }
        val nonDefaultsCount = template.matchers.size - defaultsCount
        val materializedTemplate = when {
            defaultsCount == 0 -> template
            // we only materialize defaults when other matchers are satisfied
            trace.countNoDefaultMatching(template) == nonDefaultsCount -> materializer.materialize(trace, template)
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
            matcher is DefaultValueMatcher<*> -> "[?]"
            else -> "[-]"
        }
        val statusLine = "$status ${parameter.name}:"
        val matcherRendered = matcher?.let(matcherRenderer::render) ?: "null"
        append(statusLine)
        when {
            matches -> appendLine(" $matcherRendered ~ ${valueRenderer.render(value)}")
            matcher is DefaultValueMatcher<*> -> {
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

    private fun CallTrace.countNoDefaultMatching(template: CallTemplate): Int = args.count { arg ->
        val matcher = template.matchers[arg.parameter.name]
        if (matcher is DefaultValueMatcher<*>) return@count false
        matcher?.matches(arg.value) == true
    }
}
