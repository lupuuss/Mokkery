package dev.mokkery.internal.context

import dev.mokkery.MockMode
import dev.mokkery.context.MokkeryContext
import dev.mokkery.context.require
import dev.mokkery.MokkeryCallScope
import dev.mokkery.internal.MokkeryInstanceId
import dev.mokkery.internal.MokkeryInstanceScope
import dev.mokkery.internal.ObjectIsNotMockException
import dev.mokkery.internal.ObjectIsNotSpyException
import dev.mokkery.internal.utils.bestName
import dev.mokkery.internal.utils.mokkeryRuntimeError
import kotlin.reflect.KClass

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
    val interceptedTypes: List<InterceptedTypeSpec>
    val thisRef: Any

    companion object Key : MokkeryContext.Key<MokkeryInstanceSpec> {

        fun create(
            id: MokkeryInstanceId,
            thisRef: Any,
            interceptedTypes: List<KClass<*>>,
            typeArguments: List<List<KClass<*>>>,
            mode: MockMode?,
            spiedObject: Any?
        ): MokkeryInstanceSpec {
            val types = interceptedTypes.mapIndexed { index, it -> InterceptedTypeSpec(it, typeArguments[index]) }
            return when {
                mode != null && spiedObject == null -> MokkeryMockSpec(id, thisRef, types, mode)
                mode == null && spiedObject != null -> MokkerySpySpec(id, thisRef, types, spiedObject)
                else -> mokkeryRuntimeError("Illegal state during MokkerySpec creation! Mock mode: $mode Spied object: $spiedObject")
            }
        }
    }
}

internal class MokkeryMockSpec(
    override val id: MokkeryInstanceId,
    override val thisRef: Any,
    override val interceptedTypes: List<InterceptedTypeSpec>,
    val mode: MockMode,
) : MokkeryInstanceSpec {

    override fun toString(): String = "MokkeryMockSpec(" +
            "id='$id', " +
            "interceptedTypes=[${interceptedTypes.joinToString { it.toString() }}], " +
            "thisRef={...}, " +
            "mode=$mode)"

}

internal class MokkerySpySpec(
    override val id: MokkeryInstanceId,
    override val thisRef: Any,
    override val interceptedTypes: List<InterceptedTypeSpec>,
    val spiedObject: Any
) : MokkeryInstanceSpec {

    override fun toString(): String = "MokkerySpySpec(" +
            "id='$id', " +
            "interceptedTypes=[${interceptedTypes.joinToString { it.toString() }}], " +
            "thisRef={...}, " +
            "spiedObject=${spiedObject.let { "hash(${it.hashCode()})" }})"

}

internal class InterceptedTypeSpec(val type: KClass<*>, val arguments: List<KClass<*>>) {

    override fun toString(): String = buildString {
        append(type.bestName())
        if (arguments.isNotEmpty()) {
            append("<")
            append(arguments.joinToString { it.bestName() })
            append(">")
        }
    }
}
