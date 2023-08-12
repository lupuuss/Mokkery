package dev.mokkery.test

import dev.mokkery.internal.MokkeryInterceptorScope
import dev.mokkery.internal.dynamic.MokkeryScopeLookup

internal class TestMokkeryScopeLookup(
    private val resolveFun: (Any?) -> MokkeryInterceptorScope? = { it as MokkeryInterceptorScope ? }
) : MokkeryScopeLookup {
    override fun register(obj: Any?, scope: MokkeryInterceptorScope) = error("")

    override fun resolve(obj: Any?): MokkeryInterceptorScope? = resolveFun(obj)

    override fun reverseResolve(obj: MokkeryInterceptorScope): Any = obj
}
