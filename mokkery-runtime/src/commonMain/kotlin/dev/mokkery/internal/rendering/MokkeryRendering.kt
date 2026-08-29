package dev.mokkery.internal.rendering

import dev.mokkery.MokkeryCallScope
import dev.mokkery.configurer.minusAssign
import dev.mokkery.configurer.plusAssign
import dev.mokkery.context.MokkeryContext
import dev.mokkery.context.require
import dev.mokkery.internal.MokkeryCollection
import dev.mokkery.internal.MokkeryInstanceId
import dev.mokkery.internal.matcher.CallEntry
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
import dev.mokkery.internal.utils.asListOrNull
import dev.mokkery.internal.utils.unsafeCast
import dev.mokkery.matcher.ArgMatcher
import dev.mokkery.rendering.MokkeryRenderingScope
import dev.mokkery.rendering.Renderer
import dev.mokkery.rendering.argMatcherRenderer
import dev.mokkery.rendering.descriptionRenderer
import dev.mokkery.rendering.renderOrToString

internal val MokkeryRenderingScope.mokkeryCollection: MokkeryCollection
    get() = mokkeryContext.require(MokkeryCollectionAware).collection

internal val MokkeryRenderingScope.aliases: AliasMokkeryCollection?
    get() = mokkeryContext[UseAliases]?.aliases

internal val MokkeryRenderingScope.functionRenderer: Renderer<FunctionRenderDescriptor>
    get() = mokkeryContext.require(MokkeryRendering.functionKey)

internal val MokkeryRenderingScope.instanceIdRenderer: Renderer<MokkeryInstanceId>
    get() = mokkeryContext.require(MokkeryRendering.instanceIdKey)

internal val MokkeryRenderingScope.callDescriptorRenderer: Renderer<CallRenderDescriptor>
    get() = mokkeryContext.require(MokkeryRendering.callDescriptorKey)

internal val MokkeryRenderingScope.callTemplateRenderer: Renderer<CallTemplate>
    get() = mokkeryContext.require(MokkeryRendering.callTemplateKey)

internal val MokkeryRenderingScope.callEntryRenderer: Renderer<CallEntry>
    get() = mokkeryContext.require(MokkeryRendering.callEntryKey)

internal val MokkeryRenderingScope.renderingFactory: MokkeryRendering.Factory
    get() = mokkeryContext.require(MokkeryRendering.Factory)


internal fun MokkeryRenderingConfigurer.receiverRendering(enabled: Boolean) {
    when (enabled) {
        true -> this -= DisableReceiverRendering
        false -> this += DisableReceiverRendering
    }
}

internal fun MokkeryRenderingConfigurer.mokkeryCollection(value: MokkeryCollection) {
    this += MokkeryCollectionAware(value)
}

internal fun MokkeryRenderingConfigurer.useAliases(value: MokkeryCollection, nameShortener: NameShortener) {
    this += UseAliases(value, nameShortener)
}

internal object MokkeryRendering {

    val default by lazy {
        MokkeryContext.memoized {
            +Factory.Default
            +descriptionImpl
            +argMatcherImpl
            +functionImpl
            +instanceIdImpl
            +callDescriptorImpl
            +callEntryImpl
            +callTemplateImpl
            +callScopeImpl
        }
    }

    val descriptionKey by Renderer.key<Any?>()
    val argMatcherKey by Renderer.key<ArgMatcher<*>>()
    val functionKey by Renderer.key<FunctionRenderDescriptor>()
    val instanceIdKey by Renderer.key<MokkeryInstanceId>()
    val callDescriptorKey by Renderer.key<CallRenderDescriptor>()
    val callEntryKey by Renderer.key<CallEntry>()
    val callTemplateKey by Renderer.key<CallTemplate>()
    val callScopeKey by Renderer.key<MokkeryCallScope>()

    val descriptionImpl: Renderer<Any?> = DescriptionRenderer()
    val argMatcherImpl: Renderer<ArgMatcher<*>> = ArgMatcherRenderer()
    val functionImpl: Renderer<FunctionRenderDescriptor> = FunctionRenderer()
    val instanceIdImpl: Renderer<MokkeryInstanceId> = MokkeryInstanceIdRenderer()
    val callDescriptorImpl: Renderer<CallRenderDescriptor> = CallDescriptorRenderer()
    val callEntryImpl: Renderer<CallEntry> = CallEntryRenderer()
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
    override fun render(value: MokkeryInstanceId): String = (scope.aliases?.mapOriginalToAlias(value) ?: value).toString()
}

private class CallDescriptorRenderer : Renderer<CallRenderDescriptor> {

    override val key get() = MokkeryRendering.callDescriptorKey

    context(scope: MokkeryRenderingScope)
    override fun render(value: CallRenderDescriptor) = buildString {
        if (scope.mokkeryContext[DisableReceiverRendering] == null) {
            append(scope.instanceIdRenderer.render(value.receiver))
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
        is ArgumentRenderDescriptor.Matcher -> scope.argMatcherRenderer.render(argument.matcher)
        is ArgumentRenderDescriptor.Value -> scope.descriptionRenderer.render(argument.arg.value)
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
    override fun render(value: MokkeryCallScope): String = scope.callDescriptorRenderer.render(value.asCallRenderDescriptor())
}

private class CallTemplateRenderer : Renderer<CallTemplate> {

    override val key get() = MokkeryRendering.callTemplateKey

    context(scope: MokkeryRenderingScope)
    override fun render(value: CallTemplate): String = scope.callDescriptorRenderer.render(value.asCallRenderDescriptor())
}

private class CallEntryRenderer : Renderer<CallEntry> {

    override val key get() = MokkeryRendering.callEntryKey

    context(scope: MokkeryRenderingScope)
    override fun render(value: CallEntry): String = scope.callDescriptorRenderer.render(value.asCallRenderDescriptor())
}
