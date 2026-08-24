package dev.mokkery.test

import dev.mokkery.context.CallArgument
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
    context: MokkeryContext = MokkeryContext.Empty,
) = MokkeryBlockingCallScope(testCallContext(T::class, typeName, sequence, name, args, context))

internal inline fun <reified T> testSuspendCallScope(
    typeName: String = "mock",
    sequence: Long = 1,
    name: String = "call",
    args: List<CallArgument> = emptyList(),
    context: MokkeryContext = MokkeryContext.Empty,
) = MokkerySuspendCallScope(testCallContext(T::class, typeName, sequence, name, args, context))

internal fun testCallContext(
    returnType: KClass<*>,
    typeName: String,
    sequence: Long,
    name: String,
    args: List<CallArgument>,
    context: MokkeryContext,
): MokkeryContext {
    val function = fakeFunction(
        name = name,
        parameters = args.map(CallArgument::parameter),
        returnType = returnType,
    )
    return TestMokkeryInstanceScope(typeName, sequence, functions = listOf(function))
        .mokkeryContext
        .plus(FunctionCall(function, args))
        .plus(context)
}
