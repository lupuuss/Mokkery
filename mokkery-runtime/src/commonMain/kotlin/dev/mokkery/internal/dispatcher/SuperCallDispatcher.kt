package dev.mokkery.internal.dispatcher

import kotlin.reflect.KClass

// mock/spy instance implements it if super calls available
@PublishedApi
internal interface SuperCallDispatcher {

    fun mokkeryCallSuperTypes(memberId: Int): List<KClass<*>>

    fun mokkeryDispatchSuperCall(memberId: Int, superIndex: Int, args: List<Any?>): Any?

    suspend fun mokkeryDispatchSuperCallSuspend(memberId: Int, superIndex: Int, args: List<Any?>): Any?
}

