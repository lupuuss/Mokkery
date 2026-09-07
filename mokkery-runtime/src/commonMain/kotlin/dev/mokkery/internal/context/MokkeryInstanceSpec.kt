package dev.mokkery.internal.context

import dev.mokkery.MockMode
import dev.mokkery.MokkeryCallScope
import dev.mokkery.MokkeryInstanceScope
import dev.mokkery.context.MokkeryContext
import dev.mokkery.context.require
import dev.mokkery.internal.MType
import dev.mokkery.internal.MokkeryCollection
import dev.mokkery.internal.MokkeryInstanceId
import dev.mokkery.internal.ObjectIsNotMockException
import dev.mokkery.internal.ObjectIsNotSpyException
import dev.mokkery.internal.contracts.CoreContract
import dev.mokkery.internal.mokkeryRuntimeError
import dev.mokkery.internal.requireInstanceScope
import dev.mokkery.internal.toMType

internal val MokkeryCallScope.instanceSpec: MokkeryInstanceSpec
    get() = mokkeryContext.require(MokkeryInstanceSpec)

internal val MokkeryInstanceScope.instanceSpec: MokkeryInstanceSpec
    get() = mokkeryContext.require(MokkeryInstanceSpec)

internal fun MokkeryInstanceSpec.requireMock(): MokkeryMockSpec {
    if (this !is MokkeryMockSpec) throw ObjectIsNotMockException(thisRef)
    return this
}

internal fun MokkeryInstanceSpec.requireSpy(): MokkerySpySpec {
    if (this !is MokkerySpySpec) throw ObjectIsNotSpyException(thisRef)
    return this
}

internal sealed interface MokkeryInstanceSpec : MokkeryContext.Element {

    override val key get() = Key

    val id: MokkeryInstanceId
    val interceptedTypes: List<MType>
    val thisRef: Any
    val collection: MokkeryCollection

    companion object Key : MokkeryContext.Key<MokkeryInstanceSpec> {

        fun create(
            id: MokkeryInstanceId,
            thisRef: Any,
            contract: CoreContract,
            mode: MockMode?,
            spiedObject: Any?
        ): MokkeryInstanceSpec = when {
            mode != null && spiedObject == null -> MokkeryMockSpec(id, thisRef, contract, mode)
            mode == null && spiedObject != null -> MokkerySpySpec(id, thisRef, contract, spiedObject)
            else -> mokkeryRuntimeError("Illegal state during MokkerySpec creation! Mock mode: $mode Spied object: $spiedObject")
        }
    }
}

internal data class MokkeryMockSpec(
    override val id: MokkeryInstanceId,
    override val thisRef: Any,
    val contract: CoreContract,
    val mode: MockMode,
    override val collection: MokkeryCollection = SelfMokkeryCollection(thisRef, id)
) : MokkeryInstanceSpec {

    override val interceptedTypes by lazy { contract.interceptedTypes() }

    override fun toString(): String = "MokkeryMockSpec(" +
            "id='$id', " +
            "interceptedTypes=[${interceptedTypes.joinToString { it.toString() }}], " +
            "thisRef={...}, " +
            "mode=$mode)"

}

internal data class MokkerySpySpec(
    override val id: MokkeryInstanceId,
    override val thisRef: Any,
    val contract: CoreContract,
    val spiedObject: Any,
    override val collection: MokkeryCollection = SelfMokkeryCollection(thisRef, id)
) : MokkeryInstanceSpec {

    override val interceptedTypes by lazy { contract.interceptedTypes() }

    override fun toString(): String = "MokkerySpySpec(" +
            "id='$id', " +
            "interceptedTypes=[${interceptedTypes.joinToString { it.toString() }}], " +
            "thisRef={...}, " +
            "spiedObject=${spiedObject.let { "hash(${it.hashCode()})" }})"

}

private fun CoreContract.interceptedTypes() = mokkeryInterceptedTypes.mapIndexed { index, type ->
    type.toMType(mokkeryTypeArguments.getOrElse(index) { emptyList() })
}

private class SelfMokkeryCollection(
    private val thisRef: Any,
    private val id: MokkeryInstanceId,
) : MokkeryCollection {

    override val ids = setOf(id)

    override val scopes: Collection<MokkeryInstanceScope>
        get() = listOf(thisRef.requireInstanceScope())

    override fun getScopeOrNull(id: MokkeryInstanceId): MokkeryInstanceScope? = when (this.id) {
        id -> thisRef.requireInstanceScope()
        else -> null
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is MokkeryCollection) return false
        return this.ids == other.ids
    }

    override fun hashCode(): Int = this.ids.hashCode()

    override fun toString(): String = "MokkeryCollection[${ids.single()}]"
}
