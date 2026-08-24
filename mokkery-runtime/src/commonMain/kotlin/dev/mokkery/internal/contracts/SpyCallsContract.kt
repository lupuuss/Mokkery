package dev.mokkery.internal.contracts

// spy instance implements it
@PublishedApi
internal interface SpyCallsContract : InstanceContract {

    fun mokkerySpyCall(id: Long, args: List<Any?>): Any?

    suspend fun mokkerySpyCallSuspend(id: Long, args: List<Any?>): Any?
}
