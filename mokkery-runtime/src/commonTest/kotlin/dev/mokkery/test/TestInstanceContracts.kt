package dev.mokkery.test

import dev.mokkery.context.Function
import dev.mokkery.internal.contracts.DefaultsContract
import dev.mokkery.internal.contracts.InstanceContract
import dev.mokkery.internal.contracts.InstanceContractsProvider
import dev.mokkery.internal.contracts.CoreContract
import dev.mokkery.internal.contracts.SpyCallsContract
import dev.mokkery.internal.contracts.SuperCallsContract
import dev.mokkery.internal.utils.unsafeCast
import kotlin.reflect.KClass

internal class TestInstanceContracts(
    private val functionId: Long = 0,
    private val interceptedTypes: List<KClass<*>> = emptyList(),
    private val typeArguments: List<List<KClass<*>>> = emptyList(),
    private val functions: List<Function> = emptyList(),
    private val supers: Map<KClass<*>, (List<Any?>) -> Any?> = emptyMap(),
    private val suspendSupers: Map<KClass<*>, suspend (List<Any?>) -> Any?> = emptyMap(),
    private val spied: ((List<Any?>) -> Any?)? = null,
    private val suspendSpied: (suspend (List<Any?>) -> Any?)? = null,
    private val defaultsExtractor: Any? = null,
) : InstanceContractsProvider, SpyCallsContract, SuperCallsContract, DefaultsContract, CoreContract {

    override val mokkeryInterceptedTypes: List<KClass<*>> get() = interceptedTypes

    override val mokkeryTypeArguments: List<List<KClass<*>>> get() = typeArguments

    override fun <T : InstanceContract> find(contract: KClass<T>): T? = this.unsafeCast()

    override fun mokkeryFunction(id: Long): Function? = functions.find { it.id.value == id }

    override fun mokkeryCreateExtractor(functionId: Long): Any = defaultsExtractor!!

    override fun mokkerySuperTypes(id: Long): List<KClass<*>> {
        checkFunctionId(id)
        return supers.keys.plus(suspendSupers.keys).toList()
    }

    override fun mokkerySuperCall(id: Long, superIndex: Int, args: List<Any?>): Any? = supers
        .getValue(mokkerySuperTypes(id)[superIndex])
        .invoke(args)

    override suspend fun mokkerySuperCallSuspend(id: Long, superIndex: Int, args: List<Any?>): Any? = suspendSupers
        .getValue(mokkerySuperTypes(id)[superIndex])
        .invoke(args)

    override fun mokkerySpyCall(id: Long, args: List<Any?>): Any? {
        checkFunctionId(id)
        return spied!!.invoke(args)
    }

    override suspend fun mokkerySpyCallSuspend(id: Long, args: List<Any?>): Any? {
        checkFunctionId(id)
        return suspendSpied!!.invoke(args)
    }

    private fun checkFunctionId(id: Long) {
        if (id != functionId) error("Expected dispatch for function id $functionId, but was $id!")
    }
}
