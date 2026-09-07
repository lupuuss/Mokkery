@file:Suppress("unused")

package dev.mokkery.internal

import dev.mokkery.MokkeryBlockingCallScope
import dev.mokkery.MokkeryCallScope
import dev.mokkery.MokkeryInstanceScope
import dev.mokkery.MokkerySuspendCallScope
import dev.mokkery.call
import dev.mokkery.context.Function
import dev.mokkery.context.MokkeryContext
import dev.mokkery.context.lazyFunctionCall
import dev.mokkery.internal.context.callInterceptor
import dev.mokkery.internal.context.functions
import dev.mokkery.internal.contracts.contracts
import dev.mokkery.internal.contracts.superCalls
import kotlin.reflect.KClass

@PublishedApi
internal fun MokkeryInstanceScope.interceptCall(
    id: Long,
    vararg args: Any?,
): Any? = callInterceptor.intercept(blockingCallScope(Function.Id(id), args.asList()))

@PublishedApi
internal suspend fun MokkeryInstanceScope.interceptCallSuspend(
    id: Long,
    vararg args: Any?,
): Any? = callInterceptor.intercept(suspendCallScope(Function.Id(id), args.asList()))

internal fun MokkeryCallScope.availableSuperCallTypes(): List<KClass<*>> = contracts
    .superCalls
    ?.mokkerySuperTypes(call.function.id.value)
    .orEmpty()

internal fun MokkeryBlockingCallScope.withContext(
    with: MokkeryContext = MokkeryContext.Empty
): MokkeryBlockingCallScope = when {
    with === MokkeryContext.Empty -> this
    else -> MokkeryBlockingCallScope(this.mokkeryContext + with)
}

internal fun MokkerySuspendCallScope.withContext(
    with: MokkeryContext = MokkeryContext.Empty
): MokkerySuspendCallScope = when {
    with === MokkeryContext.Empty -> this
    else -> MokkerySuspendCallScope(this.mokkeryContext + with)
}

internal fun MokkeryInstanceScope.blockingCallScope(
    id: Function.Id,
    args: List<Any?>,
): MokkeryBlockingCallScope = MokkeryBlockingCallScope(callContext(id, args))

internal fun MokkeryInstanceScope.suspendCallScope(
    id: Function.Id,
    args: List<Any?>,
): MokkerySuspendCallScope = MokkerySuspendCallScope(callContext(id, args))

internal fun MokkeryBlockingCallScope(context: MokkeryContext = MokkeryContext.Empty): MokkeryBlockingCallScope {
    return object : MokkeryBlockingCallScope {
        override val mokkeryContext = context

        override fun toString(): String = "MokkeryBlockingCallScope(mokkeryContext=$context)"
    }
}

internal fun MokkerySuspendCallScope(context: MokkeryContext = MokkeryContext.Empty): MokkerySuspendCallScope {
    return object : MokkerySuspendCallScope {
        override val mokkeryContext = context

        override fun toString(): String = "MokkerySuspendCallScope(mokkeryContext=$context)"
    }
}

private fun MokkeryInstanceScope.callContext(
    id: Function.Id,
    args: List<Any?>,
): MokkeryContext = mokkeryContext + functions.lazyFunctionCall(id, args)
