package dev.mokkery.internal.contracts

import dev.mokkery.context.Function
import kotlin.reflect.KClass

@PublishedApi
internal interface CoreContract : InstanceContract {

    val mokkeryInterceptedTypes: List<KClass<*>>

    val mokkeryTypeArguments: List<List<KClass<*>>> get() = emptyList()

    fun mokkeryNormalizeId(id: Long): Long = id

    fun mokkeryFunction(id: Long): Function?
}
