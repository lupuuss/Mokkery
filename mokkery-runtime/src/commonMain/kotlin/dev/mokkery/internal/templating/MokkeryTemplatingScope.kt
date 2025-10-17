package dev.mokkery.internal.templating

import dev.mokkery.MokkeryScope
import dev.mokkery.context.MokkeryContext
import dev.mokkery.templating.MokkeryTemplatingScope

internal fun MokkeryScope.createTemplatingScope(): MokkeryTemplatingScope {
    return MokkeryTemplatingScope(mokkeryContext + TemplatingRegistry())
}

internal fun MokkeryTemplatingScope(
    context: MokkeryContext
): MokkeryTemplatingScope = object : MokkeryTemplatingScope {
    override val mokkeryContext = context

    override fun toString(): String = "MokkeryTemplatingScope($context)"
}
