package dev.mokkery.internal.contracts

import dev.mokkery.MokkeryCallScope
import dev.mokkery.MokkeryInstanceScope
import dev.mokkery.context.MokkeryContext
import dev.mokkery.internal.utils.unsafeCast
import kotlin.reflect.KClass

internal interface InstanceContract

internal val MokkeryCallScope.superCallsContract: SuperCallsContract?
    get() = mokkeryContext[InstanceContractsProvider]?.find(SuperCallsContract::class)

internal val MokkeryCallScope.spyCallsContract: SpyCallsContract?
    get() = mokkeryContext[InstanceContractsProvider]?.find(SpyCallsContract::class)

internal val MokkeryInstanceScope.defaultsContract: DefaultsContract?
    get() = mokkeryContext[InstanceContractsProvider]?.find(DefaultsContract::class)

internal interface InstanceContractsProvider : MokkeryContext.Element {

    override val key: Key get() = Key

    fun <T : InstanceContract> find(contract: KClass<T>): T?

    companion object Key : MokkeryContext.Key<InstanceContractsProvider>
}

internal fun InstanceContractsProvider(vararg refs: Any?): InstanceContractsProvider {
    return InstanceContractsProviderImpl(refs.filterNotNull())
}

private class InstanceContractsProviderImpl(
    private val refs: List<Any>
) : InstanceContractsProvider {

    override fun <T : InstanceContract> find(contract: KClass<T>): T? = refs
        .find { contract.isInstance(it) }
        ?.unsafeCast()

}
