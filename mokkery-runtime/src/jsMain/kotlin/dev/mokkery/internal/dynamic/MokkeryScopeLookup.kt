package dev.mokkery.internal.dynamic

import dev.mokkery.internal.MokkeryInterceptorScope
import dev.mokkery.internal.WeakMap

internal actual fun MokkeryScopeLookup(): MokkeryScopeLookup = JsMokkeryScopeLookup

internal object JsMokkeryScopeLookup : MokkeryScopeLookup {

    private val mapping = WeakMap<Any, MokkeryInterceptorScope>()
    private val reverseMapping = WeakMap<MokkeryInterceptorScope, Any>()
    override fun register(obj: Any?, scope: MokkeryInterceptorScope) {
        mapping[obj ?: return] = scope
        reverseMapping[scope] = obj
    }
    override fun resolve(obj: Any?): MokkeryInterceptorScope? {
        if (obj is MokkeryInterceptorScope) return obj
        return mapping[obj ?: return null]
    }

    override fun reverseResolve(obj: MokkeryInterceptorScope): Any? = reverseMapping[obj]
}
