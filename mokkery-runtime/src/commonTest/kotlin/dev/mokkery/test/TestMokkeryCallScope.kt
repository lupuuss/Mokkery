package dev.mokkery.test

import dev.mokkery.context.CallArgument
import dev.mokkery.context.Function
import dev.mokkery.context.FunctionCall
import dev.mokkery.context.MokkeryContext
import dev.mokkery.interceptor.MokkeryBlockingCallScope
import dev.mokkery.interceptor.MokkerySuspendCallScope
import dev.mokkery.internal.context.AssociatedFunctions
import kotlin.reflect.KClass

internal inline fun <reified T> testBlockingCallScope(
    selfId: String = "mock@1",
    name: String = "call",
    args: List<CallArgument> = emptyList(),
    supers: Map<KClass<*>, kotlin.Function<Any?>> = emptyMap(),
    spiedFunction: kotlin.Function<Any?>? = null,
    context: MokkeryContext = MokkeryContext.Empty,
) = MokkeryBlockingCallScope(
    testCallContext(
        T::class,
        selfId,
        name,
        args,
        supers,
        spiedFunction,
        context
    )
)

internal inline fun <reified T> testSuspendCallScope(
    selfId: String = "mock@1",
    name: String = "call",
    args: List<CallArgument> = emptyList(),
    supers: Map<KClass<*>, kotlin.Function<Any?>> = emptyMap(),
    spiedFunction: kotlin.Function<Any?>? = null,
    context: MokkeryContext = MokkeryContext.Empty,
) = MokkerySuspendCallScope(
    testCallContext(
        T::class,
        selfId,
        name,
        args,
        supers,
        spiedFunction,
        context
    )
)

internal fun testCallContext(
    returnType: KClass<*>,
    selfId: String,
    name: String,
    args: List<CallArgument>,
    supers: Map<KClass<*>, kotlin.Function<Any?>>,
    spiedFunction: kotlin.Function<Any?>?,
    context: MokkeryContext,
) = TestMokkeryInstanceScope(selfId).mokkeryContext
    .plus(FunctionCall(Function(name, args.map { it.parameter }, returnType), args))
    .plus(AssociatedFunctions(supers, spiedFunction))
    .plus(context)
