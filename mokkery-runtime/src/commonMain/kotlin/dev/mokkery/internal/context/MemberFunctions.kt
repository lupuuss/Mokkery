package dev.mokkery.internal.context

import dev.mokkery.MokkeryInstanceScope
import dev.mokkery.context.Function
import dev.mokkery.context.MokkeryContext
import dev.mokkery.context.require
import dev.mokkery.internal.contracts.CoreContract
import dev.mokkery.internal.mokkeryRuntimeError
import kotlinx.atomicfu.atomic
import kotlinx.atomicfu.update

internal interface MemberFunctions : MokkeryContext.Element {

    override val key: Key get() = Key

    fun normalizeId(id: Function.Id): Function.Id

    operator fun get(id: Function.Id): Function

    fun getOrNull(id: Function.Id): Function?

    companion object Key : MokkeryContext.Key<MemberFunctions> {

        fun cached(contract: CoreContract): MemberFunctions = CachedMemberFunctions(contract)
    }
}

internal val MokkeryInstanceScope.functions: MemberFunctions
    get() = mokkeryContext.require(MemberFunctions)

private class CachedMemberFunctions(
    private val contract: CoreContract,
) : MemberFunctions {

    private val cache = atomic(emptyMap<Function.Id, Function>())

    override fun normalizeId(id: Function.Id): Function.Id = Function.Id(contract.mokkeryNormalizeId(id.value))

    override fun get(
        id: Function.Id
    ): Function = getOrNull(id) ?: mokkeryRuntimeError("Function with id $id not found!")

    override fun getOrNull(id: Function.Id): Function? {
        cache.value[id]?.let { return it }
        val func = contract.mokkeryFunction(id.value) ?: return null
        cache.update { it + (id to func) }
        return func
    }
}
