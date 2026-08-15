package dev.mokkery.test

import dev.mokkery.context.CallArgument
import dev.mokkery.context.Function
import dev.mokkery.context.FunctionCall
import dev.mokkery.context.MokkeryContext
import dev.mokkery.internal.MokkeryBlockingCallScope
import dev.mokkery.internal.MokkerySuspendCallScope
import kotlin.reflect.KClass

internal inline fun <reified T> testBlockingCallScope(
    typeName: String = "mock",
    sequence: Long = 1,
    name: String = "call",
    args: List<CallArgument> = emptyList(),
    functionId: Int = 0,
    context: MokkeryContext = MokkeryContext.Empty,
) = MokkeryBlockingCallScope(
    testCallContext(
        T::class,
        typeName,
        sequence,
        name,
        args,
        functionId,
        context
    )
)

internal inline fun <reified T> testSuspendCallScope(
    typeName: String = "mock",
    sequence: Long = 1,
    name: String = "call",
    args: List<CallArgument> = emptyList(),
    functionId: Int = 0,
    context: MokkeryContext = MokkeryContext.Empty,
) = MokkerySuspendCallScope(
    testCallContext(
        T::class,
        typeName,
        sequence,
        name,
        args,
        functionId,
        context
    )
)

internal fun testCallContext(
    returnType: KClass<*>,
    typeName: String,
    sequence: Long,
    name: String,
    args: List<CallArgument>,
    functionId: Int,
    context: MokkeryContext,
) = TestMokkeryInstanceScope(typeName, sequence).mokkeryContext
    .plus(FunctionCall(Function(name, args.map { it.parameter }, returnType, functionId), args))
    .plus(context)
