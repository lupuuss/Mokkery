package dev.mokkery.internal.dynamic

import dev.mokkery.internal.MokkeryInterceptorScope
import dev.mokkery.internal.mokkeryRuntimeError

internal interface MokkeryScopeLookup {
    fun register(obj: Any?, scope: MokkeryInterceptorScope)

    fun resolve(obj: Any?): MokkeryInterceptorScope?

    fun reverseResolve(obj: MokkeryInterceptorScope): Any?

    companion object {

        val current = MokkeryScopeLookup()
    }
}

internal expect fun MokkeryScopeLookup(): MokkeryScopeLookup

internal object StaticMokkeryScopeLookup : MokkeryScopeLookup {
    override fun register(obj: Any?, scope: MokkeryInterceptorScope): Nothing {
        mokkeryRuntimeError("Registering MokkeryInterceptorScope on non-JS platforms is not supported!")
    }
    override fun resolve(obj: Any?): MokkeryInterceptorScope? = obj as? MokkeryInterceptorScope
    override fun reverseResolve(obj: MokkeryInterceptorScope): Any = obj
}
