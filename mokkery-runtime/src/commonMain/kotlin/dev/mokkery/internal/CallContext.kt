package dev.mokkery.internal

import dev.mokkery.internal.tracing.CallArg
import kotlin.reflect.KClass

internal data class CallContext(
    val self: MokkeryInterceptorScope,
    val name: String,
    val returnType: KClass<*>,
    val args: List<CallArg>
) {

    override fun toString() = callToString(self.id, name, args)
}
