package dev.mokkery.test

import dev.mokkery.internal.templating.CallTemplate
import dev.mokkery.context.CallArgument
import dev.mokkery.context.Function
import dev.mokkery.internal.MokkeryInstanceId
import dev.mokkery.context.FunctionCall
import dev.mokkery.internal.matcher.CallEntry
import dev.mokkery.internal.tracing.CallTrace
import dev.mokkery.matcher.ArgMatcher
import kotlin.reflect.KClass

internal fun fakeFunction(
    name: String = "call",
    id: Long = name.hashCode().toLong(),
    parameters: List<Function.Parameter> = emptyList(),
    returnType: KClass<*> = Unit::class,
) = Function(Function.Id(id), name, parameters, returnType)

internal fun fakeCallTemplate(
    vararg matchers: ArgMatcher<Any?>,
    typeName: String = "mock",
    instanceId: Long = 1,
    name: String = "call",
    functionId: Long = name.hashCode().toLong(),
): CallTemplate = CallTemplate(
    instanceId = MokkeryInstanceId(typeName, instanceId),
    functionId = Function.Id(functionId),
    matchers = matchers.toList(),
)

internal inline fun <reified T> fakeFunParam(
    name: String,
    isVararg: Boolean = false
) = Function.Parameter(name, T::class, isVararg)

internal inline fun <reified T> fakeCallArg(
    value: T,
    name: String = "arg",
    isVararg: Boolean = false
) = CallArgument(value, Function.Parameter(name, T::class, isVararg))

internal fun fakeCallTrace(
    traceId: Long = 1,
    instanceId: Long = 1,
    typeName: String = "mock",
    name: String = "call$traceId",
    functionId: Long = name.hashCode().toLong(),
    args: List<Any?> = emptyList()
) = CallTrace(
    id = CallTrace.Id(traceId),
    instanceId = MokkeryInstanceId(typeName, instanceId),
    functionId = Function.Id(functionId),
    args = args,
)

internal fun fakeCallEntry(
    typeName: String = "mock",
    id: Long = 1,
    name: String = "call",
    functionId: Long = name.hashCode().toLong(),
    args: List<Any?> = emptyList(),
) = CallEntry(
    instanceId = MokkeryInstanceId(typeName, id),
    functionId = Function.Id(functionId),
    args = args,
)

internal fun fakeFunctionCall(
    functionName: String = "fakeFunction",
    returnType: KClass<*> = Unit::class,
    args: List<CallArgument> = emptyList(),
    id: Long = 0,
): FunctionCall = FunctionCall(
    function = fakeFunction(functionName, id, args.map(CallArgument::parameter), returnType),
    args = args,
)
