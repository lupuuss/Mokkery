package dev.mokkery.plugin.diagnostics

import org.jetbrains.kotlin.diagnostics.rendering.CommonRenderers
import org.jetbrains.kotlin.diagnostics.rendering.ContextDependentRenderer
import org.jetbrains.kotlin.diagnostics.rendering.DiagnosticParameterRenderer
import org.jetbrains.kotlin.diagnostics.rendering.Renderer
import org.jetbrains.kotlin.fir.types.ConeKotlinType
import org.jetbrains.kotlin.fir.types.renderReadable

fun CommonRenderers.simpleType() = Renderer<ConeKotlinType> { it.renderReadable() }

fun <A, B> CommonRenderers.pair(
    firstRenderer: DiagnosticParameterRenderer<A>,
    secondRenderer: DiagnosticParameterRenderer<B>,
): DiagnosticParameterRenderer<Pair<A, B>> = ContextDependentRenderer { (first, second), context ->
    firstRenderer.render(first, context) + ": " + secondRenderer.render(second, context)
}

fun <T> CommonRenderers.points(
    itemRenderer: DiagnosticParameterRenderer<T>
): DiagnosticParameterRenderer<Collection<T>> = ContextDependentRenderer { collection, context ->
    buildString {
        appendLine()
        collection.forEach {
            appendLine("\t" + itemRenderer.render(it, context))
        }
    }
}
