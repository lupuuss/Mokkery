package dev.mokkery.internal

import dev.mokkery.internal.tracing.CallArg
import kotlin.reflect.KClass

internal data class CallContext(
    val thisRef: MokkeryInterceptorScope,
    val name: String,
    val returnType: KClass<*>,
    val args: List<CallArg>
)
