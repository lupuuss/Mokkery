package dev.mokkery.test

import dev.mokkery.context.CallArgument
import dev.mokkery.context.Function
import dev.mokkery.context.FunctionCall
import dev.mokkery.context.MokkeryContext
import dev.mokkery.interceptor.MokkeryCallScope
import dev.mokkery.internal.context.AssociatedFunctions
import dev.mokkery.internal.context.CurrentMokkeryInstance
import kotlin.reflect.KClass

inline fun <reified T> testCallScope(
    selfId: String = "mock@1",
    name: String = "call",
    args: List<CallArgument> = emptyList(),
    supers: Map<KClass<*>, kotlin.Function<Any?>> = emptyMap(),
    spiedFunction: kotlin.Function<Any?>? = null,
    context: MokkeryContext = MokkeryContext.Empty,
): MokkeryCallScope = testCallScope(
    T::class,
    selfId,
    name,
    args,
    supers,
    spiedFunction,
    context
)

fun testCallScope(
    returnType: KClass<*>,
    selfId: String,
    name: String,
    args: List<CallArgument>,
    supers: Map<KClass<*>, kotlin.Function<Any?>>,
    spiedFunction: kotlin.Function<Any?>?,
    context: MokkeryContext,
): MokkeryCallScope = MokkeryCallScope(
    context + CurrentMokkeryInstance(TestMokkeryInstance(selfId))
            + FunctionCall(Function(name, args.map { it.parameter }, returnType), args)
            + AssociatedFunctions(supers, spiedFunction)
)
