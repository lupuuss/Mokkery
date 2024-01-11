package dev.mokkery.internal.dynamic

import dev.mokkery.internal.MokkeryInterceptorScope

internal actual fun MokkeryScopeLookup(): MokkeryScopeLookup = WasmJsMokkeryScopeLookup

internal object WasmJsMokkeryScopeLookup : MokkeryScopeLookup {

    private val mapping = mutableMapOf<Any?, MokkeryInterceptorScope>()
    private val reverseMapping = mutableMapOf<MokkeryInterceptorScope, Any?>()
    override fun register(obj: Any?, scope: MokkeryInterceptorScope) {
        mapping[obj ?: return] = scope
        reverseMapping[scope] = obj
    }
    override fun resolve(obj: Any?): MokkeryInterceptorScope? {
        if (obj is MokkeryInterceptorScope) return obj
        return mapping[obj ?: return null]
    }

    override fun reverseResolve(obj: MokkeryInterceptorScope): Any {
        return reverseMapping[obj] ?: obj
    }
}
