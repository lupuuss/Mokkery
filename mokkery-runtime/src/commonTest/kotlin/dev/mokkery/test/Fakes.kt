package dev.mokkery.test

import dev.mokkery.answering.FunctionScope
import dev.mokkery.internal.CallContext
import dev.mokkery.internal.templating.CallTemplate
import dev.mokkery.context.CallArgument
import dev.mokkery.context.Function
import dev.mokkery.internal.tracing.CallTrace
import dev.mokkery.matcher.ArgMatcher
import kotlin.reflect.KClass

fun fakeFunctionScope(
    returnType: KClass<*> = Unit::class,
    self: Any? = Unit,
    supers: Map<KClass<*>, (List<Any?>) -> Any?> = emptyMap(),
    args: List<Any?> = emptyList()
) = FunctionScope(
    returnType = returnType,
    args = args,
    self = self,
    supers = supers
)

internal fun fakeCallTemplate(
    receiver: String = "mock@1",
    name: String = "call",
    signature: String = "call()",
    matchers: Map<String, ArgMatcher<Any?>> = emptyMap(),
) = CallTemplate(
    receiver = receiver,
    name = name,
    signature = signature,
    matchers = matchers,
)

internal inline fun <reified T> fakeCallArg(
    value: T,
    name: String = "arg",
    isVararg: Boolean = false
) = CallArgument(value, Function.Parameter(name, T::class, isVararg))

internal inline fun <reified T> fakeCallContext(
    selfId: String = "mock@1",
    name: String = "call",
    args: List<CallArgument> = emptyList(),
    supers: Map<KClass<*>, (List<Any?>) -> Any?> = emptyMap(),
) = CallContext(
    instance = TestMokkeryInstance(selfId),
    name = name,
    returnType = T::class,
    args = args,
    supers = supers
)

internal fun fakeCallTrace(
    receiver: String = "mock@1",
    name: String = "call",
    args: List<CallArgument> = emptyList(),
    orderStamp: Long = 0
) = CallTrace(
    receiver = receiver,
    name = name,
    args = args,
    orderStamp = orderStamp,
)
