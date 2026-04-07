package dev.mokkery.internal.render

import dev.mokkery.MokkeryCallScope
import dev.mokkery.internal.names.AliasMokkeryCollection
import dev.mokkery.internal.render.descriptor.CallRenderDescriptor
import dev.mokkery.internal.render.descriptor.FunctionRenderDescriptor
import dev.mokkery.internal.render.descriptor.asCallRenderDescriptor
import dev.mokkery.internal.templating.CallTemplate
import dev.mokkery.internal.tracing.CallTrace

internal fun Renderers.callScope(
    renderReceiver: Boolean = true,
    aliases: AliasMokkeryCollection? = null,
) = callScope(callDescriptor(renderReceiver = renderReceiver, instanceIdRenderer = instanceId(aliases)))

internal fun Renderers.callTemplate(
    renderReceiver: Boolean = true,
    aliases: AliasMokkeryCollection? = null
) = callTemplate(callDescriptor(renderReceiver = renderReceiver, instanceIdRenderer = instanceId(aliases)))

internal fun Renderers.callTrace(
    renderReceiver: Boolean = true,
    aliases: AliasMokkeryCollection? = null
) = callTrace(callDescriptor(renderReceiver = renderReceiver, instanceIdRenderer = instanceId(aliases)))

internal fun Renderers.callScope(descriptorRenderer: Renderer<CallRenderDescriptor>) = Renderer<MokkeryCallScope> {
    descriptorRenderer.render(it.asCallRenderDescriptor())
}

internal fun Renderers.callTemplate(descriptorRenderer: Renderer<CallRenderDescriptor>) = Renderer<CallTemplate> {
    descriptorRenderer.render(it.asCallRenderDescriptor())
}

internal fun Renderers.callTrace(descriptorRenderer: Renderer<CallRenderDescriptor>) = Renderer<CallTrace> {
    descriptorRenderer.render(it.asCallRenderDescriptor())
}

internal fun Renderers.functionName(
    descriptorRenderer: Renderer<FunctionRenderDescriptor> = functionDescriptor()
) = Renderer<String> {
    descriptorRenderer.render(FunctionRenderDescriptor.parse(it))
}
