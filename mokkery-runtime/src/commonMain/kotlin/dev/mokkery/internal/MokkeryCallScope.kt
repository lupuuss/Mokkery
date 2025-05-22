@file:Suppress("unused")

package dev.mokkery.internal

import dev.mokkery.context.CallArgument
import dev.mokkery.context.Function
import dev.mokkery.context.FunctionCall
import dev.mokkery.context.MokkeryContext
import dev.mokkery.MokkeryBlockingCallScope
import dev.mokkery.MokkerySuspendCallScope
import dev.mokkery.internal.context.AssociatedFunctions
import dev.mokkery.internal.utils.copyWithReplacedKClasses
import dev.mokkery.internal.utils.takeIfImplementedOrAny
import kotlin.reflect.KClass

internal fun MokkeryBlockingCallScope(context: MokkeryContext = MokkeryContext.Empty): MokkeryBlockingCallScope {
    return object : MokkeryBlockingCallScope {
        override val mokkeryContext = context

        override fun toString(): String = "MokkeryBlockingCallScope($context)"
    }
}

internal fun MokkerySuspendCallScope(context: MokkeryContext = MokkeryContext.Empty): MokkerySuspendCallScope {
    return object : MokkerySuspendCallScope {
        override val mokkeryContext = context

        override fun toString(): String = "MokkerySuspendCallScope($context)"
    }
}

internal fun MokkeryBlockingCallScope.withContext(
    with: MokkeryContext = MokkeryContext.Empty
): MokkeryBlockingCallScope {
    return when {
        with === MokkeryContext.Empty -> this
        else -> MokkeryBlockingCallScope(this.mokkeryContext + with)
    }
}

internal fun MokkerySuspendCallScope.withContext(
    with: MokkeryContext = MokkeryContext.Empty
): MokkerySuspendCallScope {
    return when {
        with === MokkeryContext.Empty -> this
        else -> MokkerySuspendCallScope(this.mokkeryContext + with)
    }
}

internal fun MokkeryInstanceScope.createMokkeryBlockingCallScope(
    name: String,
    returnType: KClass<*>,
    args: List<CallArgument>,
    supers: Map<KClass<*>, kotlin.Function<Any?>> = emptyMap(),
    spyDelegate: kotlin.Function<Any?>? = null
) = MokkeryBlockingCallScope(createMokkeryCallContext(name, returnType, args, supers, spyDelegate))

internal fun MokkeryInstanceScope.createMokkerySuspendCallScope(
    name: String,
    returnType: KClass<*>,
    args: List<CallArgument>,
    supers: Map<KClass<*>, kotlin.Function<Any?>> = emptyMap(),
    spyDelegate: kotlin.Function<Any?>? = null
) = MokkerySuspendCallScope(createMokkeryCallContext(name, returnType, args, supers, spyDelegate))

private fun MokkeryInstanceScope.createMokkeryCallContext(
    name: String,
    returnType: KClass<*>,
    args: List<CallArgument>,
    supers: Map<KClass<*>, kotlin.Function<Any?>>,
    spyDelegate: kotlin.Function<Any?>?
): MokkeryContext {
    val safeArgs = args.copyWithReplacedKClasses()
    val call = FunctionCall(
        function = Function(
            name = name,
            parameters = args.map(CallArgument::parameter),
            returnType = returnType.takeIfImplementedOrAny()
        ),
        args = safeArgs
    )
    return mokkeryContext + call + AssociatedFunctions(supers, spyDelegate)
}
