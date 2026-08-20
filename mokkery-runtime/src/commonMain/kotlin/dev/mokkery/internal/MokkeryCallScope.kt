@file:Suppress("unused")

package dev.mokkery.internal

import dev.mokkery.context.CallArgument
import dev.mokkery.context.Function
import dev.mokkery.context.FunctionCall
import dev.mokkery.context.MokkeryContext
import dev.mokkery.MokkeryBlockingCallScope
import dev.mokkery.MokkeryCallScope
import dev.mokkery.MokkeryInstanceScope
import dev.mokkery.MokkerySuspendCallScope
import dev.mokkery.call
import dev.mokkery.internal.context.callInterceptor
import dev.mokkery.internal.contracts.superCallsContract
import dev.mokkery.internal.utils.copyWithReplacedKClasses
import dev.mokkery.internal.utils.takeIfImplementedOrAny
import kotlin.collections.orEmpty
import kotlin.reflect.KClass

@PublishedApi
internal fun MokkeryInstanceScope.interceptCall(
    name: String,
    returnType: KClass<*>,
    functionId: Int,
    vararg args: CallArgument,
): Any? = callInterceptor.intercept(blockingCallScope(name, returnType, args.asList(), functionId))

@PublishedApi
internal suspend fun MokkeryInstanceScope.interceptCallSuspend(
    name: String,
    returnType: KClass<*>,
    functionId: Int,
    vararg args: CallArgument,
): Any? = callInterceptor.intercept(suspendCallScope(name, returnType, args.asList(), functionId))


internal fun MokkeryCallScope.availableSuperCallTypes(): List<KClass<*>> = superCallsContract
    ?.mokkerySuperTypes(call.function.id)
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
    name: String,
    returnType: KClass<*>,
    args: List<CallArgument>,
    functionId: Int,
): MokkeryBlockingCallScope = MokkeryBlockingCallScope(callContext(name, returnType, args, functionId))

internal fun MokkeryInstanceScope.suspendCallScope(
    name: String,
    returnType: KClass<*>,
    args: List<CallArgument>,
    functionId: Int
): MokkerySuspendCallScope = MokkerySuspendCallScope(callContext(name, returnType, args, functionId))

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
    name: String,
    returnType: KClass<*>,
    args: List<CallArgument>,
    functionId: Int,
): MokkeryContext {
    val safeArgs = args.copyWithReplacedKClasses()
    val call = FunctionCall(
        function = Function(
            name = name,
            parameters = args.map(CallArgument::parameter),
            returnType = returnType.takeIfImplementedOrAny(),
            id = functionId,
        ),
        args = safeArgs
    )
    return mokkeryContext + call
}
