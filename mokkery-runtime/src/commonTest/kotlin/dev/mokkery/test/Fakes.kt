package dev.mokkery.test

import dev.mokkery.answering.FunctionScope
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
