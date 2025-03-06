package dev.mokkery.internal

import dev.mokkery.internal.utils.WeakMap
import dev.mokkery.internal.utils.WeakRef
import dev.mokkery.internal.utils.weaken

internal actual fun MokkeryScopeLookup(): MokkeryScopeLookup = JsMokkeryScopeLookup

internal object JsMokkeryScopeLookup : MokkeryScopeLookup {

    private val mapping = WeakMap<Any, MokkeryInstanceScope>()
    private val reverseMapping = WeakMap<MokkeryInstanceScope, WeakRef<Any>>()
    override fun registerScope(obj: Any?, scope: MokkeryInstanceScope) {
        mapping[obj ?: return] = scope
        reverseMapping[scope] = obj.weaken()
    }
    override fun resolveScopeOrNull(obj: Any?): MokkeryInstanceScope? {
        if (obj is MokkeryInstanceScope) return obj
        return mapping[obj ?: return null]
    }

    override fun resolveInstanceOrNull(obj: MokkeryInstanceScope): Any {
        return reverseMapping[obj]?.value ?: obj
    }
}
