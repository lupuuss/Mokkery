package dev.mokkery.internal.rendering

import dev.mokkery.MokkeryCallScope
import dev.mokkery.context.MokkeryContext
import dev.mokkery.context.memoized
import dev.mokkery.context.require
import dev.mokkery.internal.MokkeryCollection
import dev.mokkery.internal.MokkeryInstanceId
import dev.mokkery.internal.names.AliasMokkeryCollection
import dev.mokkery.internal.names.NameShortener
import dev.mokkery.internal.names.withShorterNames
import dev.mokkery.internal.rendering.descriptor.ArgumentRenderDescriptor
import dev.mokkery.internal.rendering.descriptor.CallRenderDescriptor
import dev.mokkery.internal.rendering.descriptor.FunctionRenderDescriptor
import dev.mokkery.internal.rendering.descriptor.GetterRenderDescriptor
import dev.mokkery.internal.rendering.descriptor.SetterRenderDescriptor
import dev.mokkery.internal.rendering.descriptor.asCallRenderDescriptor
import dev.mokkery.internal.templating.CallTemplate
import dev.mokkery.internal.tracing.CallTrace
import dev.mokkery.internal.utils.asListOrNull
import dev.mokkery.internal.utils.unsafeCast
import dev.mokkery.matcher.ArgMatcher
import dev.mokkery.rendering.MokkeryRenderingScope
import dev.mokkery.rendering.renderOrToString

context(scope: MokkeryRenderingScope)
internal val mokkeryCollection: MokkeryCollection
    get() = scope.mokkeryContext.require(MokkeryCollectionAware).collection

context(scope: MokkeryRenderingScope)
internal val aliases: AliasMokkeryCollection?
    get() = scope.mokkeryContext[UseAliases]?.aliases

context(scope: MokkeryRenderingScope)
internal val descriptionRenderer: Renderer<Any?>
    get() = scope.mokkeryContext.require(MokkeryRendering.descriptionKey)

context(scope: MokkeryRenderingScope)
internal val argMatcherRenderer: Renderer<ArgMatcher<*>>
    get() = scope.mokkeryContext.require(MokkeryRendering.argMatcherKey)

context(scope: MokkeryRenderingScope)
internal val functionRenderer: Renderer<FunctionRenderDescriptor>
    get() = scope.mokkeryContext.require(MokkeryRendering.functionKey)

context(scope: MokkeryRenderingScope)
internal val instanceIdRenderer: Renderer<MokkeryInstanceId>
    get() = scope.mokkeryContext.require(MokkeryRendering.instanceIdKey)

context(scope: MokkeryRenderingScope)
internal val callDescriptorRenderer: Renderer<CallRenderDescriptor>
    get() = scope.mokkeryContext.require(MokkeryRendering.callDescriptorKey)

context(scope: MokkeryRenderingScope)
internal val callTemplateRenderer: Renderer<CallTemplate>
    get() = scope.mokkeryContext.require(MokkeryRendering.callTemplateKey)

context(scope: MokkeryRenderingScope)
internal val callTraceRenderer: Renderer<CallTrace>
    get() = scope.mokkeryContext.require(MokkeryRendering.callTraceKey)

context(scope: MokkeryRenderingScope)
internal val callScopeRenderer: Renderer<MokkeryCallScope>
    get() = scope.mokkeryContext.require(MokkeryRendering.callScopeKey)

context(scope: MokkeryRenderingScope)
internal val factory: MokkeryRendering.Factory
    get() = scope.mokkeryContext.require(MokkeryRendering.Factory)


internal fun RenderingConfigurer.receiverRendering(enabled: Boolean) {
    when (enabled) {
        true -> -DisableReceiverRendering
        false -> +DisableReceiverRendering
    }
}

internal fun RenderingConfigurer.mokkeryCollection(value: MokkeryCollection) {
    +MokkeryCollectionAware(value)
}

internal fun RenderingConfigurer.useAliases(value: MokkeryCollection, nameShortener: NameShortener) {
    +UseAliases(value, nameShortener)
}

internal object MokkeryRendering {

    val default by lazy {
        Factory.Default
            .plus(descriptionImpl)
            .plus(argMatcherImpl)
            .plus(functionImpl)
            .plus(instanceIdImpl)
            .plus(callDescriptorImpl)
            .plus(callTraceImpl)
            .plus(callTemplateImpl)
            .plus(callScopeImpl)
            .memoized()
    }

    val descriptionKey by Renderer.key<Any?>()
    val argMatcherKey by Renderer.key<ArgMatcher<*>>()
    val functionKey by Renderer.key<FunctionRenderDescriptor>()
    val instanceIdKey by Renderer.key<MokkeryInstanceId>()
    val callDescriptorKey by Renderer.key<CallRenderDescriptor>()
    val callTraceKey by Renderer.key<CallTrace>()
    val callTemplateKey by Renderer.key<CallTemplate>()
    val callScopeKey by Renderer.key<MokkeryCallScope>()

    val descriptionImpl: Renderer<Any?> = DescriptionRenderer()
    val argMatcherImpl: Renderer<ArgMatcher<*>> = ArgMatcherRenderer()
    val functionImpl: Renderer<FunctionRenderDescriptor> = FunctionRenderer()
    val instanceIdImpl: Renderer<MokkeryInstanceId> = MokkeryInstanceIdRenderer()
    val callDescriptorImpl: Renderer<CallRenderDescriptor> = CallDescriptorRenderer()
    val callTraceImpl: Renderer<CallTrace> = CallTraceRenderer()
    val callTemplateImpl: Renderer<CallTemplate> = CallTemplateRenderer()
    val callScopeImpl: Renderer<MokkeryCallScope> = CallScopeRenderer()


    internal interface Factory : MokkeryContext.Element {

        override val key get() = Key

        fun <T> points(point: String = "*", item: Renderer<T>): Renderer<List<T>>

        companion object Key : MokkeryContext.Key<Factory>

        object Default : Factory {

            override fun <T> points(
                point: String,
                item: Renderer<T>
            ): Renderer<List<T>> = PointsRenderer(point, item)
        }
    }
}

private object DisableReceiverRendering : MokkeryContext.Element, MokkeryContext.Key<DisableReceiverRendering> {
    override val key get() = this
}

private class MokkeryCollectionAware(val collection: MokkeryCollection) : MokkeryContext.Element {

    override val key = Key

    companion object Key : MokkeryContext.Key<MokkeryCollectionAware>
}

private class UseAliases(val collection: MokkeryCollection, nameShortener: NameShortener) : MokkeryContext.Element {

    val aliases: AliasMokkeryCollection by lazy { collection.withShorterNames(nameShortener) }

    override val key = Key

    companion object Key : MokkeryContext.Key<UseAliases>
}

private class DescriptionRenderer : Renderer<Any?> {

    override val key get() = MokkeryRendering.descriptionKey

    context(scope: MokkeryRenderingScope)
    override fun render(value: Any?): String = when (value) {
        null -> "null"
        is String -> "\"$value\""
        is Function<*> -> "{...}"
        else -> value.asListOrNull()?.toString() ?: value.toString()
    }
}

private class ArgMatcherRenderer : Renderer<ArgMatcher<*>> {

    override val key get() = MokkeryRendering.argMatcherKey

    context(scope: MokkeryRenderingScope)
    override fun render(value: ArgMatcher<*>): String = value.renderOrToString()
}

private class FunctionRenderer : Renderer<FunctionRenderDescriptor> {

    override val key get() = MokkeryRendering.functionKey

    context(scope: MokkeryRenderingScope)
    override fun render(value: FunctionRenderDescriptor) = when (value) {
        is GetterRenderDescriptor -> "get ${value.name}"
        is SetterRenderDescriptor -> "set ${value.name}"
        else -> value.name
    }
}

private class MokkeryInstanceIdRenderer : Renderer<MokkeryInstanceId> {

    override val key get() = MokkeryRendering.instanceIdKey

    context(scope: MokkeryRenderingScope)
    override fun render(value: MokkeryInstanceId): String = (aliases?.mapOriginalToAlias(value) ?: value).toString()
}

private class CallDescriptorRenderer : Renderer<CallRenderDescriptor> {

    override val key get() = MokkeryRendering.callDescriptorKey

    context(scope: MokkeryRenderingScope)
    override fun render(value: CallRenderDescriptor) = buildString {
        if (scope.mokkeryContext[DisableReceiverRendering] == null) {
            append(instanceIdRenderer.render(value.receiver))
            append(".")
        }
        when (value.function) {
            is GetterRenderDescriptor -> {
                if (value.arguments.isNotEmpty()) {
                    appendNamedArguments(value.arguments)
                    append(".")
                }
                append(value.function.name)
            }
            is SetterRenderDescriptor -> {
                val setArg = value.arguments.last()
                val extArguments = value.arguments.dropLast(1)
                if (extArguments.isNotEmpty()) {
                    appendNamedArguments(extArguments)
                    append(".")
                }
                append(value.function.name)
                append(" = ")
                append(render(setArg))
            }
            else -> {
                append(value.function.name)
                appendNamedArguments(value.arguments)
            }
        }
    }

    context(scope: MokkeryRenderingScope)
    private fun StringBuilder.appendNamedArguments(
        args: List<ArgumentRenderDescriptor>
    ) {
        append("(")
        append(args.joinToString { "${it.parameter.name} = ${render(it)}" })
        append(")")
    }

    context(scope: MokkeryRenderingScope)
    private fun render(argument: ArgumentRenderDescriptor): String = when (argument) {
        is ArgumentRenderDescriptor.Matcher -> argMatcherRenderer.render(argument.matcher)
        is ArgumentRenderDescriptor.Value -> descriptionRenderer.render(argument.arg.value)
    }
}

private class PointsRenderer<T>(
    private val point: String,
    private val item: Renderer<T>,
) : Renderer<List<T>> {

    override val key = Renderer.Key<List<T>>("points")

    context(scope: MokkeryRenderingScope)
    override fun render(value: List<T>): String = buildString {
        value.forEach {
            append(point)
            append(" ")
            appendLine(item.render(it.unsafeCast()))
        }
    }
}

private class CallScopeRenderer : Renderer<MokkeryCallScope> {

    override val key get() = MokkeryRendering.callScopeKey

    context(scope: MokkeryRenderingScope)
    override fun render(value: MokkeryCallScope): String = callDescriptorRenderer.render(value.asCallRenderDescriptor())
}

private class CallTemplateRenderer : Renderer<CallTemplate> {

    override val key get() = MokkeryRendering.callTemplateKey

    context(scope: MokkeryRenderingScope)
    override fun render(value: CallTemplate): String = callDescriptorRenderer.render(value.asCallRenderDescriptor())
}

private class CallTraceRenderer : Renderer<CallTrace> {

    override val key get() = MokkeryRendering.callTraceKey

    context(scope: MokkeryRenderingScope)
    override fun render(value: CallTrace): String = callDescriptorRenderer.render(value.asCallRenderDescriptor())
}
