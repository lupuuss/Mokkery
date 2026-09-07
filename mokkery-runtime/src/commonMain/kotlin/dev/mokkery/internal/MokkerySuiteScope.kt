@file:Suppress("unused")
package dev.mokkery.internal

import dev.mokkery.MokkeryScope
import dev.mokkery.MokkerySuiteScope
import dev.mokkery.context.MokkeryContext
import dev.mokkery.internal.context.MokkeryInstancesRegistry
import dev.mokkery.internal.interceptor.forkedHooksOrEmpty

@PublishedApi
internal fun MokkeryScope.suiteScope(
    context: MokkeryContext = MokkeryContext.Empty
): MokkerySuiteScope = MokkerySuiteScopeImpl(suiteContext(context))

@PublishedApi
internal fun MokkeryScope.suiteContext(
    context: MokkeryContext = MokkeryContext.Empty
): MokkeryContext = mokkeryContext + forkedHooksOrEmpty() + MokkeryInstancesRegistry() + context

private class MokkerySuiteScopeImpl(override val mokkeryContext: MokkeryContext) : MokkerySuiteScope {

    override fun toString(): String = "MokkerySuiteScope(mokkeryContext=$mokkeryContext)"
}
