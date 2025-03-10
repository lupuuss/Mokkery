package dev.mokkery.test

import dev.mokkery.answering.FunctionScope
import dev.mokkery.internal.calls.CallTemplate
import dev.mokkery.context.CallArgument
import dev.mokkery.context.Function
import dev.mokkery.internal.MockId
import dev.mokkery.context.FunctionCall
import dev.mokkery.internal.calls.CallTrace
import dev.mokkery.matcher.ArgMatcher
import kotlin.reflect.KClass

fun fakeFunctionScope(
    returnType: KClass<*> = Unit::class,
    self: Any? = Unit,
    supers: Map<KClass<*>, (List<Any?>) -> Any?> = emptyMap(),
    args: List<Any?> = emptyList(),
    classSupertypes: List<KClass<*>> = listOf(Unit::class)
) = FunctionScope(
    returnType = returnType,
    args = args,
    self = self,
    supers = supers,
    classSupertypes = classSupertypes
)

internal fun fakeCallTemplate(
    typeName: String = "mock",
    id: Long = 1,
    name: String = "call",
    signature: String = "call()",
    matchers: Map<String, ArgMatcher<Any?>> = emptyMap(),
) = CallTemplate(
    mockId = MockId(typeName, id),
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
    typeName: String = "mock",
    id: Long = 1,
    name: String = "call",
    args: List<CallArgument> = emptyList(),
    orderStamp: Long = 0
) = CallTrace(
    mockId = MockId(typeName, id),
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
