package dev.mokkery.test

import dev.mokkery.answering.FunctionScope
import kotlin.reflect.KClass

fun fakeFunctionScope(
    returnType: KClass<*> = Unit::class,
    vararg args: Any?
) = FunctionScope(
    returnType = returnType,
    args = args.toList()
)
