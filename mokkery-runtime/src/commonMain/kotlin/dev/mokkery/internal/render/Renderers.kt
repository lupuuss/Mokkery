package dev.mokkery.internal.render

import dev.mokkery.internal.MokkeryInstanceId
import dev.mokkery.internal.names.AliasMokkeryCollection
import dev.mokkery.internal.names.aliasTemplate
import dev.mokkery.internal.names.aliasTrace
import dev.mokkery.internal.templating.CallTemplate
import dev.mokkery.internal.tracing.CallTrace
import dev.mokkery.internal.utils.description

internal object Renderers {

    val toString = Renderer<Any?> { it.toString() }
    val description = Renderer<Any?> { it.description() }

    fun callTemplateAlias(from: AliasMokkeryCollection) = Renderer<CallTemplate> {
        from.aliasTemplate(it).toString()
    }

    fun callTraceAlias(from: AliasMokkeryCollection) = Renderer<CallTrace> {
        from.aliasTrace(it).toString()
    }

    fun instanceIdAlias(from: AliasMokkeryCollection) = Renderer<MokkeryInstanceId> {
        from.mapOriginalToAlias(it).toString()
    }

    fun <T> points(
        point: String = "*",
        item: Renderer<T>
    ): Renderer<List<T>> = Renderer { value ->
        buildString {
            value.forEach {
                append(point)
                append(" ")
                appendLine(item.render(it))
            }
        }
    }
}
