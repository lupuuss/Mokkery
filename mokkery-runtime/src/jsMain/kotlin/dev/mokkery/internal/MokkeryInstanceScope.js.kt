package dev.mokkery.internal

import dev.mokkery.MockMode
import dev.mokkery.MokkeryInstanceScope
import dev.mokkery.MokkeryMockScope
import dev.mokkery.MokkeryScope
import dev.mokkery.MokkerySpyScope
import dev.mokkery.context.MokkeryContext
import dev.mokkery.context.require
import dev.mokkery.internal.context.MokkeryInstanceSpec
import dev.mokkery.internal.context.MokkeryMockSpec
import dev.mokkery.internal.context.MokkerySpySpec
import kotlin.reflect.KClass

internal actual val Any.mokkeryScope: MokkeryInstanceScope?
    get() = this as? MokkeryInstanceScope ?: jsFunctionMokkeryScope

@Suppress("unused")
internal fun MokkeryScope.setupMokkeryInstanceForJsFunction(
    typeName: String,
    interceptedType: KClass<*>,
    typeArguments: List<KClass<*>> = emptyList(),
    thisRef: Any,
    mode: MockMode?,
    spiedObject: Any?,
    block: (Any.() -> Unit)?,
) {
    val context = instanceContext(
        mode = mode,
        typeName = typeName,
        interceptedTypes = listOf(interceptedType),
        typeArguments = listOf(typeArguments),
        thisRef = thisRef,
        spiedObject = spiedObject
    )
    val scope = MokkeryJsFunScope(context)
    thisRef.jsFunctionMokkeryScope = scope
    thisRef.asDynamic().toString = scope::toString
    scope.finalizeMokkeryInstance(
        thisRef = thisRef,
        block = block,
    )
}

internal inline var Any.jsFunctionMokkeryScope: MokkeryInstanceScope?
    get() = this.asDynamic()._mokkeryScope as? MokkeryInstanceScope
    set(value) {
        this.asDynamic()._mokkeryScope = value
    }

private fun MokkeryJsFunScope(
    context: MokkeryContext
): MokkeryJsFunScope = when (context.require(MokkeryInstanceSpec)) {
    is MokkeryMockSpec -> MokkeryJsFunMockScope(context)
    is MokkerySpySpec -> MokkeryJsFunSpyScope(context)
}

private interface MokkeryJsFunScope : MokkeryInstanceScope

private class MokkeryJsFunMockScope(
    override val mokkeryContext: MokkeryContext
) : MokkeryJsFunScope, MokkeryMockScope {

    override fun toString(): String = instanceIdString
}

private class MokkeryJsFunSpyScope(
    override val mokkeryContext: MokkeryContext
) : MokkeryJsFunScope, MokkerySpyScope {

    override fun toString(): String = instanceIdString
}
