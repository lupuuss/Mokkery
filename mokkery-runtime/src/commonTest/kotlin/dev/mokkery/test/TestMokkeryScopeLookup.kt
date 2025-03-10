package dev.mokkery.test

import dev.mokkery.internal.MokkeryInstanceScope
import dev.mokkery.internal.MokkeryScopeLookup

internal class TestMokkeryScopeLookup(
    private val resolveFun: (Any?) -> MokkeryInstanceScope? = { it as MokkeryInstanceScope ? }
) : MokkeryScopeLookup {
    override fun registerScope(obj: Any?, scope: MokkeryInstanceScope) = error("")

    override fun resolveScopeOrNull(obj: Any?): MokkeryInstanceScope? = resolveFun(obj)

    override fun resolveInstanceOrNull(obj: MokkeryInstanceScope): Any = obj
}
