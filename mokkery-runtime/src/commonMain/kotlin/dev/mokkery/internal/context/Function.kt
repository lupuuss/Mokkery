@file:Suppress("unused")

package dev.mokkery.internal.context

import dev.mokkery.context.Function
import dev.mokkery.internal.utils.takeIfImplementedOrAny
import kotlin.reflect.KClass

@PublishedApi
internal fun createFunction(
    id: Long,
    name: String,
    parameters: List<Function.Parameter>,
    returnType: KClass<*>,
): Function = Function(
    id = Function.Id(id),
    name = name,
    parameters = parameters,
    returnType = returnType.takeIfImplementedOrAny(),
)

@PublishedApi
internal fun createFunctionParameter(
    name: String,
    type: KClass<*>,
    isVararg: Boolean,
): Function.Parameter = Function.Parameter(
    name = name,
    type = type.takeIfImplementedOrAny(),
    isVararg = isVararg,
)
