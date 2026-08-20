package dev.mokkery.internal.contracts

import kotlin.reflect.KClass

// mock/spy instance implements it if super calls available
@PublishedApi
internal interface SuperCallsContract : InstanceContract {

    fun mokkerySuperTypes(memberId: Int): List<KClass<*>>

    fun mokkerySuperCall(memberId: Int, superIndex: Int, args: List<Any?>): Any?

    suspend fun mokkerySuperCallSuspend(memberId: Int, superIndex: Int, args: List<Any?>): Any?
}

