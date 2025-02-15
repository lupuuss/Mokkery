package dev.mokkery.internal.context

import dev.mokkery.MokkeryScope
import dev.mokkery.context.MokkeryContext

internal val MokkeryScope.suiteName: String?
    get() = mokkeryContext[SuiteName]?.name

internal data class SuiteName(val name: String) : MokkeryContext.Element {

    override val key = Key

    companion object Key : MokkeryContext.Key<SuiteName>
}
