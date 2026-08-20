package dev.mokkery.internal.contracts

// spy instance implements it
@PublishedApi
internal interface SpyCallsContract : InstanceContract {

    fun mokkerySpyCall(memberId: Int, args: List<Any?>): Any?

    suspend fun mokkerySpyCallSuspend(memberId: Int, args: List<Any?>): Any?
}
