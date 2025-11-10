package dev.mokkery.test

import dev.mokkery.internal.templating.CallTemplate
import dev.mokkery.context.CallArgument
import dev.mokkery.context.Function
import dev.mokkery.internal.MokkeryInstanceId
import dev.mokkery.context.FunctionCall
import dev.mokkery.internal.tracing.CallTrace
import dev.mokkery.matcher.ArgMatcher
import kotlin.reflect.KClass

internal fun fakeCallTemplate(
    vararg matchers: Pair<Function.Parameter, ArgMatcher<Any?>>,
    typeName: String = "mock",
    id: Long = 1,
    name: String = "call",
): CallTemplate {
    return CallTemplate(
        instanceId = MokkeryInstanceId(typeName, id),
        name = name,
        parameters = matchers.map { it.first },
        matchers = matchers.associate { it.first.name to it.second },
    )
}

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
    typeName: String = "mock",
    id: Long = 1,
    name: String = "call",
    args: List<CallArgument> = emptyList(),
    orderStamp: Long = 0
) = CallTrace(
    instanceId = MokkeryInstanceId(typeName, id),
    name = name,
    args = args,
    orderStamp = orderStamp,
)

internal fun fakeFunctionCall(
    functionName: String = "fakeFunction",
    returnType: KClass<*> = Unit::class,
    args: List<CallArgument> = emptyList()
): FunctionCall {
    return  FunctionCall(
        function = Function(functionName, args.map(CallArgument::parameter), returnType),
        args = args
    )
}
