package dev.mokkery.internal

import dev.mokkery.internal.tracing.CallArg
import kotlin.reflect.KClass

internal data class CallContext(
    val scope: MokkeryInterceptorScope,
    val name: String,
    val returnType: KClass<*>,
    val args: List<CallArg>,
    val supers: Map<KClass<*>, (List<Any?>) -> Any?> = emptyMap(),
    val spyDelegate: Any? = null // regular function or suspend function (List<Any?>) -> Any?
) {

    override fun toString() = callToString(scope.id, name, args)
}
