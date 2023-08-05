package dev.mokkery.test

import dev.mokkery.internal.MokkeryInterceptorScope
import dev.mokkery.internal.dynamic.MokkeryScopeLookup

internal class TestMokkeryScopeLookup : MokkeryScopeLookup {
    override fun register(obj: Any?, scope: MokkeryInterceptorScope) = error("")

    override fun resolve(obj: Any?): MokkeryInterceptorScope? = obj as MokkeryInterceptorScope?

    override fun reverseResolve(obj: MokkeryInterceptorScope): Any = obj
}
