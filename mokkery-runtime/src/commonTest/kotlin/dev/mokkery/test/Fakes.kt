package dev.mokkery.test

import dev.mokkery.answering.FunctionScope
import dev.mokkery.internal.templating.CallTemplate
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
    receiver: String = "Receiver",
    name: String = "call",
    signature: String = "call(i: Int)",
    matchers: Map<String, ArgMatcher<Any?>> = mapOf("i" to ArgMatcher.Equals(1)),
) = CallTemplate(
    receiver = receiver,
    name = name,
    signature = signature,
    matchers = matchers,
)
