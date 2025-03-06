package dev.mokkery.internal

import dev.mokkery.internal.utils.WeakMap
import dev.mokkery.internal.utils.WeakRef
import dev.mokkery.internal.utils.weaken

internal actual fun MokkeryInstanceLookup(): MokkeryInstanceLookup = JsMokkeryInstanceLookup

internal object JsMokkeryInstanceLookup : MokkeryInstanceLookup {

    private val mapping = WeakMap<Any, MokkeryInstanceScope>()
    private val reverseMapping = WeakMap<MokkeryInstanceScope, WeakRef<Any>>()
    override fun register(obj: Any?, instance: MokkeryInstanceScope) {
        mapping[obj ?: return] = instance
        reverseMapping[instance] = obj.weaken()
    }
    override fun resolve(obj: Any?): MokkeryInstanceScope? {
        if (obj is MokkeryInstanceScope) return obj
        return mapping[obj ?: return null]
    }

    override fun reverseResolve(obj: MokkeryInstanceScope): Any {
        return reverseMapping[obj]?.value ?: obj
    }
}
