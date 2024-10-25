package dev.mokkery.test

import dev.mokkery.answering.FunctionScope
import dev.mokkery.internal.calls.CallTemplate
import dev.mokkery.context.CallArgument
import dev.mokkery.context.Function
import dev.mokkery.internal.calls.CallTrace
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
