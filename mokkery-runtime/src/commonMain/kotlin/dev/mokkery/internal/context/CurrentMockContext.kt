package dev.mokkery.internal.context

import dev.mokkery.MockMode
import dev.mokkery.context.MokkeryContext
import dev.mokkery.context.require
import dev.mokkery.interceptor.MokkeryCallInterceptor
import dev.mokkery.interceptor.MokkeryCallScope
import dev.mokkery.internal.MokkeryInstanceScope
import dev.mokkery.internal.interceptor.MokkeryKind
import kotlin.reflect.KClass

internal val MokkeryCallScope.currentMockContext: CurrentMockContext
    get() = mokkeryContext.require(CurrentMockContext)

internal val MokkeryInstanceScope.currentMockContext: CurrentMockContext
    get() = mokkeryContext.require(CurrentMockContext)

internal class CurrentMockContext(
    val id: String,
    val mode: MockMode,
    val kind: MokkeryKind,
    val interceptedTypes: List<KClass<*>>,
    val typeArguments: List<KClass<*>>,
    val self: MokkeryInstanceScope,
    val spiedObject: Any?,
    val interceptor: MokkeryCallInterceptor
) : MokkeryContext.Element {

    override fun toString(): String {
        return "CurrentMockContext(id='$id', mode=$mode, kind=$kind, interceptedTypes=$interceptedTypes, self=$self)"
    }

    override val key = Key

    companion object Key : MokkeryContext.Key<CurrentMockContext>
}
