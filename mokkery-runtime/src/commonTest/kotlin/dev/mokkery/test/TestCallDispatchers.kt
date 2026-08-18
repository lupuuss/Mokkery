package dev.mokkery.test

import dev.mokkery.internal.dispatcher.CallDispatchers
import dev.mokkery.internal.dispatcher.SpyCallDispatcher
import dev.mokkery.internal.dispatcher.SuperCallDispatcher
import kotlin.reflect.KClass

internal class TestCallDispatchers(
    private val functionId: Int = 0,
    private val supers: Map<KClass<*>, (List<Any?>) -> Any?> = emptyMap(),
    private val suspendSupers: Map<KClass<*>, suspend (List<Any?>) -> Any?> = emptyMap(),
    private val spied: ((List<Any?>) -> Any?)? = null,
    private val suspendSpied: (suspend (List<Any?>) -> Any?)? = null,
) : CallDispatchers, SpyCallDispatcher, SuperCallDispatcher {

    override val spyDispatcher: SpyCallDispatcher?
        get() = takeIf { spied != null || suspendSpied != null }

    override val superDispatcher: SuperCallDispatcher?
        get() = takeIf { supers.isNotEmpty() || suspendSupers.isNotEmpty() }

    override fun mokkeryCallSuperTypes(memberId: Int): List<KClass<*>> {
        checkFunctionId(memberId)
        return supers.keys.plus(suspendSupers.keys).toList()
    }

    override fun mokkeryDispatchSuperCall(memberId: Int, superIndex: Int, args: List<Any?>): Any? = supers
        .getValue(mokkeryCallSuperTypes(memberId)[superIndex])
        .invoke(args)

    override suspend fun mokkeryDispatchSuperCallSuspend(memberId: Int, superIndex: Int, args: List<Any?>): Any? = suspendSupers
        .getValue(mokkeryCallSuperTypes(memberId)[superIndex])
        .invoke(args)

    override fun mokkeryDispatchSpyCall(memberId: Int, args: List<Any?>): Any? {
        checkFunctionId(memberId)
        return spied!!.invoke(args)
    }

    override suspend fun mokkeryDispatchSpyCallSuspend(memberId: Int, args: List<Any?>): Any? {
        checkFunctionId(memberId)
        return suspendSpied!!.invoke(args)
    }

    private fun checkFunctionId(memberId: Int) {
        if (memberId != functionId) error("Expected dispatch for function id $functionId, but was $memberId!")
    }
}
