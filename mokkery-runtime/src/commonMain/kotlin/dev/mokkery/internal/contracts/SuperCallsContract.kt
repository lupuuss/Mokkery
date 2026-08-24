package dev.mokkery.internal.contracts

import kotlin.reflect.KClass

// mock/spy instance implements it if super calls available
@PublishedApi
internal interface SuperCallsContract : InstanceContract {

    fun mokkerySuperTypes(id: Long): List<KClass<*>>

    fun mokkerySuperCall(id: Long, superIndex: Int, args: List<Any?>): Any?

    suspend fun mokkerySuperCallSuspend(id: Long, superIndex: Int, args: List<Any?>): Any?
}

