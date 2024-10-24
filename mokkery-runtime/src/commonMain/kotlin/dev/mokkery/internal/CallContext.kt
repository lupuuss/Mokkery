package dev.mokkery.internal

import dev.mokkery.context.CallArgument
import kotlin.reflect.KClass

internal class CallContext(
    val instance: MokkeryInstance,
    val name: String,
    returnType: KClass<*>,
    args: List<CallArgument>,
    val supers: Map<KClass<*>, (List<Any?>) -> Any?> = emptyMap(),
    val spyDelegate: Any? = null // regular function or suspend function (List<Any?>) -> Any?
) {

    // filters out unimplemented KClasses on K/N
    val returnType: KClass<*> = returnType.takeIfImplementedOrAny()
    val args: List<CallArgument> = args.copyWithReplacedKClasses()

    override fun toString() = callToString(instance.id, name, args)
}
