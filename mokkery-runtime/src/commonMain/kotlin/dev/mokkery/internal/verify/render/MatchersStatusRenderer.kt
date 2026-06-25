package dev.mokkery.internal.verify.render

import dev.mokkery.context.CallArgument
import dev.mokkery.internal.context.tools
import dev.mokkery.internal.matcher.DefaultValuesMatcher
import dev.mokkery.internal.rendering.Renderer
import dev.mokkery.internal.rendering.argMatcherRenderer
import dev.mokkery.internal.rendering.descriptionRenderer
import dev.mokkery.internal.rendering.mokkeryCollection
import dev.mokkery.internal.templating.CallTemplate
import dev.mokkery.internal.tracing.CallTrace
import dev.mokkery.matcher.ArgMatcher
import dev.mokkery.rendering.MokkeryRenderingScope

internal object MatchersStatusRenderer : Renderer<Pair<CallTemplate, CallTrace>> {

    override val key get() = VerifyRendering.matcherStatus

    context(scope: MokkeryRenderingScope)
    override fun render(value: Pair<CallTemplate, CallTrace>): String {
        val (template, trace) = value
        val defaultMatchersCount = template.matchers.values.count { it is DefaultValuesMatcher }
        val nonDefaultMatchersCount = template.matchers.size - defaultMatchersCount
        val materializedTemplate = when {
            defaultMatchersCount == 0 -> template
            // we only materialize defaults when other matchers are satisfied
            trace.countNonDefaultMatching(template) == nonDefaultMatchersCount -> scope.tools
                .defaultsMaterializerFactory
                .create(mokkeryCollection)
                .materialize(trace, template)
            else -> template
        }
        return buildString {
            trace.args.forEach {
                append(it.describeMatchingAgainst(materializedTemplate.matchers[it.parameter.name]))
            }
        }
    }

    context(scope: MokkeryRenderingScope)
    private fun CallArgument.describeMatchingAgainst(matcher: ArgMatcher<Any?>?): String = buildString {
        val matches = matcher?.matches(value) == true
        val status = when {
            matches -> "[+]"
            matcher is DefaultValuesMatcher -> "[?]"
            else -> "[-]"
        }
        val statusLine = "$status ${parameter.name}:"
        val matcherRendered = matcher?.let { argMatcherRenderer.render(it) } ?: "null"
        append(statusLine)
        when {
            matches -> appendLine(" $matcherRendered ~ ${descriptionRenderer.render(value)}")
            matcher is DefaultValuesMatcher -> {
                appendLine()
                appendLine("   expect: default() => Cannot be determined, because other matchers don't match!")
                appendLine("   actual: ${descriptionRenderer.render(value)}")
            }
            else -> {
                appendLine()
                appendLine("   expect: $matcherRendered")
                appendLine("   actual: ${descriptionRenderer.render(value)}")
            }
        }
    }

    private fun CallTrace.countNonDefaultMatching(template: CallTemplate): Int = args.count { arg ->
        val matcher = template.matchers[arg.parameter.name]
        if (matcher is DefaultValuesMatcher) return@count false
        matcher?.matches(arg.value) == true
    }
}
