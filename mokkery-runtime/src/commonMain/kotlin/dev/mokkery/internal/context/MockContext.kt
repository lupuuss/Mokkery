package dev.mokkery.internal.context

import dev.mokkery.MockMode
import dev.mokkery.context.MokkeryContext
import dev.mokkery.context.require
import dev.mokkery.interceptor.MokkeryCallInterceptor
import dev.mokkery.interceptor.MokkeryCallScope
import dev.mokkery.internal.MokkeryInstanceScope
import dev.mokkery.internal.interceptor.MokkeryKind
import kotlin.reflect.KClass

internal val MokkeryCallScope.mockContext: MockContext
    get() = mokkeryContext.require(MockContext)

internal val MokkeryInstanceScope.mockContext: MockContext
    get() = mokkeryContext.require(MockContext)

internal class MockContext(
    val id: String,
    val mode: MockMode,
    val kind: MokkeryKind,
    val interceptedTypes: List<KClass<*>>,
    val typeArguments: List<KClass<*>>,
    val spiedObject: Any?,
    val thisInstanceScope: MokkeryInstanceScope,
    val interceptor: MokkeryCallInterceptor
) : MokkeryContext.Element {

    override val key = Key

    override fun toString(): String = "MockContext(" +
            "id='$id', " +
            "mode=$mode, " +
            "kind=$kind, " +
            "interceptedTypes=$interceptedTypes, " +
            "typeArguments=$typeArguments, " +
            "spiedObject={hash=${spiedObject.hashCode()}}, " +
            "thisInstanceScopeHashCode={hash=${thisInstanceScope.hashCode()}}, " +
            "interceptor=$interceptor)"

    companion object Key : MokkeryContext.Key<MockContext>
}
