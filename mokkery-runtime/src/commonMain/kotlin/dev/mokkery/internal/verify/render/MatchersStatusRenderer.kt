package dev.mokkery.internal.verify.render

import dev.mokkery.internal.render.Renderer
import dev.mokkery.internal.render.ToStringRenderer
import dev.mokkery.internal.render.ValueDescriptionRenderer
import dev.mokkery.internal.templating.CallTemplate
import dev.mokkery.internal.tracing.CallArg
import dev.mokkery.internal.tracing.CallTrace
import dev.mokkery.matcher.ArgMatcher

internal class MatchersStatusRenderer(
    private val matcherRenderer: Renderer<ArgMatcher<*>> = ToStringRenderer,
    private val valueRenderer: Renderer<Any?> = ValueDescriptionRenderer
) : Renderer<Pair<CallTemplate, CallTrace>> {

    override fun render(value: Pair<CallTemplate, CallTrace>): String {
        val (template, trace) = value
        return buildString {
            trace.args.forEach {
                append(it.describeMatchingAgainst(template.matchers[it.name]))
            }
        }
    }

    private fun CallArg.describeMatchingAgainst(matcher: ArgMatcher<Any?>?): String = buildString {
        val matches = matcher?.matches(value) == true
        val status = if (matches) "[+]" else "[-]"
        val statusLine = "$status $name:"
        val matcherRendered = matcher?.let(matcherRenderer::render) ?: "null"
        append(statusLine)
        if (matches) {
            appendLine(" $matcherRendered ~ ${valueRenderer.render(value)}")
        } else {
            appendLine()
            appendLine("   expect: $matcherRendered")
            appendLine("   actual: ${valueRenderer.render(value)}")
        }
    }
}
