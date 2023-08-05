package dev.mokkery.internal.dynamic

import dev.mokkery.internal.MokkeryInterceptorScope

internal actual fun MokkeryScopeLookup(): MokkeryScopeLookup = EmptyMokkeryScopeLookup

internal object EmptyMokkeryScopeLookup : MokkeryScopeLookup {
    override fun register(obj: Any?, scope: MokkeryInterceptorScope): Nothing {
        error("Registering MokkeryInterceptorScope on non-JS platforms is not supported!")
    }
    override fun resolve(obj: Any?): MokkeryInterceptorScope? = obj as? MokkeryInterceptorScope
    override fun reverseResolve(obj: MokkeryInterceptorScope): Any = obj

}
