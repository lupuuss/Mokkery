package dev.mokkery.internal.context

import dev.mokkery.MockMode
import dev.mokkery.context.MokkeryContext
import dev.mokkery.context.require
import dev.mokkery.MokkeryCallScope
import dev.mokkery.internal.MockId
import dev.mokkery.internal.MokkeryInstanceScope
import dev.mokkery.internal.MokkeryKind
import dev.mokkery.internal.utils.bestName
import kotlin.reflect.KClass

internal val MokkeryCallScope.mockSpec: MockSpec
    get() = mokkeryContext.require(MockSpec)

internal val MokkeryInstanceScope.mockSpec: MockSpec
    get() = mokkeryContext.require(MockSpec)

internal class MockSpec(
    val id: MockId,
    val mode: MockMode,
    val kind: MokkeryKind,
    val interceptedTypes: List<TypeSpec>,
    val thisRef: Any,
    val spiedObject: Any?,
) : MokkeryContext.Element {

    override val key = Key

    override fun toString(): String = "MockSpec(" +
            "id='$id', " +
            "mode=$mode, " +
            "kind=$kind, " +
            "interceptedTypes=[${interceptedTypes.joinToString { it.toString() }}], " +
            "thisRef={...}, " +
            "spiedObject=${spiedObject?.let { "hash(${it.hashCode()})" }})"

    companion object Key : MokkeryContext.Key<MockSpec>
}

internal class TypeSpec(val type: KClass<*>, val arguments: List<KClass<*>>) {

    override fun toString(): String = buildString {
        append(type.bestName())
        if (arguments.isNotEmpty()) {
            append("<")
            append(arguments.joinToString { it.bestName() })
            append(">")
        }
    }
}
