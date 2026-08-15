package dev.mokkery.internal.dispatcher

import kotlin.reflect.KClass

@PublishedApi
internal interface MokkerySuperCallDispatcher {

    fun mokkeryCallSuperTypes(memberId: Int): List<KClass<*>>

    fun mokkeryDispatchSuperCall(memberId: Int, superIndex: Int, args: List<Any?>): Any?

    suspend fun mokkeryDispatchSuperCallSuspend(memberId: Int, superIndex: Int, args: List<Any?>): Any?
}

