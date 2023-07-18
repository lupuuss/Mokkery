package dev.mokkery.internal.dynamic

import dev.mokkery.internal.MokkeryInterceptorScope
import dev.mokkery.internal.WeakMap

internal actual fun MokkeryScopeLookup(): MokkeryScopeLookup = JsMokkeryScopeLookup

internal object JsMokkeryScopeLookup : MokkeryScopeLookup {

    private val map = WeakMap<Any, MokkeryInterceptorScope>()
    override fun register(obj: Any?, scope: MokkeryInterceptorScope) {
        map[obj ?: return] = scope
    }
    override fun resolve(obj: Any?): MokkeryInterceptorScope? {
        return map[obj ?: return null]
    }
}
