package dev.mokkery.test

import dev.mokkery.internal.MokkeryInstance
import dev.mokkery.internal.MokkeryInstanceLookup

internal class TestMokkeryInstanceLookup(
    private val resolveFun: (Any?) -> MokkeryInstance? = { it as MokkeryInstance ? }
) : MokkeryInstanceLookup {
    override fun register(obj: Any?, instance: MokkeryInstance) = error("")

    override fun resolve(obj: Any?): MokkeryInstance? = resolveFun(obj)

    override fun reverseResolve(obj: MokkeryInstance): Any = obj
}
