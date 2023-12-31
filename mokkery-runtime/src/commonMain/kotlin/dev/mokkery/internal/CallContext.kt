package dev.mokkery.internal

import dev.mokkery.internal.tracing.CallArg
import kotlin.reflect.KClass

internal data class CallContext(
    val scope: MokkeryInterceptorScope,
    val name: String,
    val returnType: KClass<*>,
    val args: List<CallArg>,
    val supers: Map<KClass<*>, (List<Any?>) -> Any?> = emptyMap(),
) {

    override fun toString() = callToString(scope.id, name, args)
}
