package dev.mokkery.test

import dev.mokkery.answering.FunctionScope
import dev.mokkery.internal.CallContext
import dev.mokkery.internal.MokkeryInterceptorScope
import dev.mokkery.internal.templating.CallTemplate
import dev.mokkery.internal.tracing.CallArg
import dev.mokkery.internal.tracing.CallTrace
import dev.mokkery.matcher.ArgMatcher
import kotlin.reflect.KClass

fun fakeFunctionScope(
    returnType: KClass<*> = Unit::class,
    self: Any? = Unit,
    vararg args: Any?
) = FunctionScope(
    returnType = returnType,
    args = args.toList(),
    self = self,
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
) = CallArg(name, T::class, value, isVararg = isVararg)

internal inline fun <reified T> fakeCallContext(
    selfId: String = "mock@1",
    name: String = "call",
    args: List<CallArg> = emptyList()
) = CallContext(
    self = TestMokkeryInterceptorScope(selfId),
    name = name,
    returnType = T::class,
    args = args
)

internal fun fakeCallTrace(
    receiver: String = "mock@1",
    name: String = "call",
    args: List<CallArg> = emptyList(),
    orderStamp: Long = 0
) = CallTrace(
    receiver = receiver,
    name = name,
    args = args,
    orderStamp = orderStamp,
)
