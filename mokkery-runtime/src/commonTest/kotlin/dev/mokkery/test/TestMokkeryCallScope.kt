package dev.mokkery.test

import dev.mokkery.context.CallArgument
import dev.mokkery.context.Function
import dev.mokkery.context.FunctionCall
import dev.mokkery.context.MokkeryContext
import dev.mokkery.internal.interceptor.MokkeryBlockingCallScope
import dev.mokkery.internal.interceptor.MokkerySuspendCallScope
import dev.mokkery.internal.context.AssociatedFunctions
import kotlin.reflect.KClass

internal inline fun <reified T> testBlockingCallScope(
    typeName: String = "mock",
    sequence: Long = 1,
    name: String = "call",
    args: List<CallArgument> = emptyList(),
    supers: Map<KClass<*>, kotlin.Function<Any?>> = emptyMap(),
    spiedFunction: kotlin.Function<Any?>? = null,
    context: MokkeryContext = MokkeryContext.Empty,
) = MokkeryBlockingCallScope(
    testCallContext(
        T::class,
        typeName,
        sequence,
        name,
        args,
        supers,
        spiedFunction,
        context
    )
)

internal inline fun <reified T> testSuspendCallScope(
    typeName: String = "mock",
    sequence: Long = 1,
    name: String = "call",
    args: List<CallArgument> = emptyList(),
    supers: Map<KClass<*>, kotlin.Function<Any?>> = emptyMap(),
    spiedFunction: kotlin.Function<Any?>? = null,
    context: MokkeryContext = MokkeryContext.Empty,
) = MokkerySuspendCallScope(
    testCallContext(
        T::class,
        typeName,
        sequence,
        name,
        args,
        supers,
        spiedFunction,
        context
    )
)

internal fun testCallContext(
    returnType: KClass<*>,
    typeName: String,
    sequence: Long,
    name: String,
    args: List<CallArgument>,
    supers: Map<KClass<*>, kotlin.Function<Any?>>,
    spiedFunction: kotlin.Function<Any?>?,
    context: MokkeryContext,
) = TestMokkeryInstanceScope(typeName, sequence).mokkeryContext
    .plus(FunctionCall(Function(name, args.map { it.parameter }, returnType), args))
    .plus(AssociatedFunctions(supers, spiedFunction))
    .plus(context)
