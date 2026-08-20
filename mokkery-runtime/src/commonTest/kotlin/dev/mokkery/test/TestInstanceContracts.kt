package dev.mokkery.test

import dev.mokkery.context.Function
import dev.mokkery.internal.contracts.DefaultsContract
import dev.mokkery.internal.contracts.InstanceContract
import dev.mokkery.internal.contracts.InstanceContractsProvider
import dev.mokkery.internal.contracts.SpyCallsContract
import dev.mokkery.internal.contracts.SuperCallsContract
import dev.mokkery.internal.utils.unsafeCast
import kotlin.reflect.KClass

internal class TestInstanceContracts(
    private val functionId: Int = 0,
    private val supers: Map<KClass<*>, (List<Any?>) -> Any?> = emptyMap(),
    private val suspendSupers: Map<KClass<*>, suspend (List<Any?>) -> Any?> = emptyMap(),
    private val spied: ((List<Any?>) -> Any?)? = null,
    private val suspendSpied: (suspend (List<Any?>) -> Any?)? = null,
    private val defaultsExtractor: Any? = null,
) : InstanceContractsProvider, SpyCallsContract, SuperCallsContract, DefaultsContract {

    override fun <T : InstanceContract> find(contract: KClass<T>): T? = this.unsafeCast()

    override fun mokkeryCreateExtractor(functionName: String, parameters: List<Function.Parameter>): Any {
        return defaultsExtractor!!
    }

    override fun mokkerySuperTypes(memberId: Int): List<KClass<*>> {
        checkFunctionId(memberId)
        return supers.keys.plus(suspendSupers.keys).toList()
    }

    override fun mokkerySuperCall(memberId: Int, superIndex: Int, args: List<Any?>): Any? = supers
        .getValue(mokkerySuperTypes(memberId)[superIndex])
        .invoke(args)

    override suspend fun mokkerySuperCallSuspend(memberId: Int, superIndex: Int, args: List<Any?>): Any? = suspendSupers
        .getValue(mokkerySuperTypes(memberId)[superIndex])
        .invoke(args)

    override fun mokkerySpyCall(memberId: Int, args: List<Any?>): Any? {
        checkFunctionId(memberId)
        return spied!!.invoke(args)
    }

    override suspend fun mokkerySpyCallSuspend(memberId: Int, args: List<Any?>): Any? {
        checkFunctionId(memberId)
        return suspendSpied!!.invoke(args)
    }

    private fun checkFunctionId(memberId: Int) {
        if (memberId != functionId) error("Expected dispatch for function id $functionId, but was $memberId!")
    }
}
