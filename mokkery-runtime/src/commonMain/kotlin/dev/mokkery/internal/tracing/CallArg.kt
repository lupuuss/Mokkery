package dev.mokkery.internal.tracing

import kotlin.reflect.KClass

internal data class CallArg(
    val name: String,
    val type: KClass<*>,
    val value: Any?,
    val isVararg: Boolean,
)
