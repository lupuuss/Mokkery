package dev.mokkery.coroutines

import dev.mokkery.answering.FunctionScope
import kotlin.reflect.KClass

@Suppress("INVISIBLE_MEMBER", "INVISIBLE_REFERENCE")
internal fun fakeFunctionScope(
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
