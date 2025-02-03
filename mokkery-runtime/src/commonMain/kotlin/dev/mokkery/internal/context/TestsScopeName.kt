package dev.mokkery.internal.context

import dev.mokkery.MokkeryScope
import dev.mokkery.context.MokkeryContext

internal val MokkeryScope.testsScopeName: String?
    get() = mokkeryContext[TestsScopeName]?.name

internal data class TestsScopeName(val name: String) : MokkeryContext.Element {

    override val key = Key

    companion object Key : MokkeryContext.Key<TestsScopeName>
}
