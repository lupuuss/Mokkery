package dev.mokkery.internal.dispatcher

// spy instance implements it
@PublishedApi
internal interface SpyCallDispatcher {

    fun mokkeryDispatchSpyCall(memberId: Int, args: List<Any?>): Any?

    suspend fun mokkeryDispatchSpyCallSuspend(memberId: Int, args: List<Any?>): Any?
}
