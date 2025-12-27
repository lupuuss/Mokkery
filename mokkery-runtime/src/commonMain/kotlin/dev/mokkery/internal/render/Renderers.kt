package dev.mokkery.internal.render

import dev.mokkery.internal.MokkeryInstanceId
import dev.mokkery.internal.names.AliasMokkeryCollection
import dev.mokkery.internal.utils.asListOrNull
import dev.mokkery.matcher.ArgMatcher

internal interface Renderers {

    val toString: Renderer<Any?>
    val description: Renderer<Any?>

    fun instanceId(aliases: AliasMokkeryCollection? = null): Renderer<MokkeryInstanceId>

    fun <T> points(point: String = "*", item: Renderer<T>): Renderer<List<T>>

    fun callDescriptor(
        instanceIdRenderer: Renderer<MokkeryInstanceId>,
        valueRenderer: Renderer<Any?> = description,
        matcherRenderer: Renderer<ArgMatcher<*>> = toString,
        renderReceiver: Boolean = true,
    ): Renderer<CallRenderDescriptor>

   companion object {

       val default = object : Renderers {

           override val toString = Renderer(Any?::toString)

           override val description = Renderer<Any?> {
               when (it) {
                   null -> "null"
                   is String -> "\"$it\""
                   is Function<*> -> "{...}"
                   else -> it.asListOrNull()?.toString() ?: it.toString()
               }
           }

           override fun instanceId(
               aliases: AliasMokkeryCollection?,
           ) = Renderer<MokkeryInstanceId> { id -> (aliases?.mapOriginalToAlias(id) ?: id).toString() }

           override fun <T> points(
               point: String,
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

           override fun callDescriptor(
               instanceIdRenderer: Renderer<MokkeryInstanceId>,
               valueRenderer: Renderer<Any?>,
               matcherRenderer: Renderer<ArgMatcher<*>>,
               renderReceiver: Boolean,
           ) = object : Renderer<CallRenderDescriptor> {

               override fun render(value: CallRenderDescriptor) = buildString {
                   if (renderReceiver) {
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

               private fun StringBuilder.appendNamedArguments(
                   args: List<ArgumentRenderDescriptor>
               ) {
                   append("(")
                   append(args.joinToString { "${it.parameter.name} = ${render(it)}" })
                   append(")")
               }

               private fun render(argument: ArgumentRenderDescriptor): String = when (argument) {
                   is ArgumentRenderDescriptor.Matcher -> matcherRenderer.render(argument.matcher)
                   is ArgumentRenderDescriptor.Value -> valueRenderer.render(argument.arg.value)
               }
           }
       }
   }
}
