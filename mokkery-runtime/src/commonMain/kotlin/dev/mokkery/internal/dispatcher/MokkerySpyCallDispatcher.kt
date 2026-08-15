package dev.mokkery.internal.dispatcher

@PublishedApi
internal interface MokkerySpyCallDispatcher {

    fun mokkeryDispatchSpyCall(memberId: Int, args: List<Any?>): Any?

    suspend fun mokkeryDispatchSpyCallSuspend(memberId: Int, args: List<Any?>): Any?
}
