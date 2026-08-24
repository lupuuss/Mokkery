package dev.mokkery.internal.contracts

import dev.mokkery.MokkeryCallScope
import dev.mokkery.MokkeryInstanceScope
import dev.mokkery.context.MokkeryContext
import dev.mokkery.context.require
import dev.mokkery.internal.mokkeryRuntimeError
import dev.mokkery.internal.utils.unsafeCast
import kotlin.reflect.KClass

internal interface InstanceContract

internal val MokkeryInstanceScope.instanceContracts: InstanceContractsProvider
    get() = mokkeryContext.require(InstanceContractsProvider)

internal val MokkeryCallScope.instanceContracts: InstanceContractsProvider
    get() = mokkeryContext.require(InstanceContractsProvider)

internal val InstanceContractsProvider.superCalls: SuperCallsContract?
    get() = find(SuperCallsContract::class)

internal val InstanceContractsProvider.spyCalls: SpyCallsContract?
    get() = find(SpyCallsContract::class)

internal val InstanceContractsProvider.defaults: DefaultsContract?
    get() = find(DefaultsContract::class)

internal val InstanceContractsProvider.memberFunctions: MemberFunctionsContract
    get() = find(MemberFunctionsContract::class) ?: mokkeryRuntimeError("Member functions contract is not available for this instance, but it should be!")

internal interface InstanceContractsProvider : MokkeryContext.Element {

    override val key: Key get() = Key

    fun <T : InstanceContract> find(contract: KClass<T>): T?

    companion object Key : MokkeryContext.Key<InstanceContractsProvider>
}

internal fun InstanceContractsProvider(
    vararg refs: Any?
): InstanceContractsProvider = InstanceContractsProviderImpl(refs.filterNotNull())

private class InstanceContractsProviderImpl(
    private val refs: List<Any>
) : InstanceContractsProvider {

    override fun <T : InstanceContract> find(contract: KClass<T>): T? = refs
        .find { contract.isInstance(it) }
        ?.unsafeCast()

}
