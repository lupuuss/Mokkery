package dev.mokkery.internal

import dev.mokkery.internal.tracing.CallArg
import kotlin.reflect.KClass

internal data class CallContext(
    val receiver: String,
    val name: String,
    val returnType: KClass<*>,
    val args: List<CallArg>
)
