package dev.mokkery.test

import dev.mokkery.internal.MokkeryInstanceScope
import dev.mokkery.internal.MokkeryInstanceLookup

internal class TestMokkeryInstanceLookup(
    private val resolveFun: (Any?) -> MokkeryInstanceScope? = { it as MokkeryInstanceScope ? }
) : MokkeryInstanceLookup {
    override fun register(obj: Any?, instance: MokkeryInstanceScope) = error("")

    override fun resolve(obj: Any?): MokkeryInstanceScope? = resolveFun(obj)

    override fun reverseResolve(obj: MokkeryInstanceScope): Any = obj
}
