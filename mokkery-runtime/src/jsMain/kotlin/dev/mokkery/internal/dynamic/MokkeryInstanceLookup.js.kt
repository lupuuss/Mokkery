package dev.mokkery.internal.dynamic

import dev.mokkery.internal.MokkeryInstance
import dev.mokkery.internal.WeakMap
import dev.mokkery.internal.WeakRef
import dev.mokkery.internal.weaken

internal actual fun MokkeryInstanceLookup(): MokkeryInstanceLookup = JsMokkeryInstanceLookup

internal object JsMokkeryInstanceLookup : MokkeryInstanceLookup {

    private val mapping = WeakMap<Any, MokkeryInstance>()
    private val reverseMapping = WeakMap<MokkeryInstance, WeakRef<Any>>()
    override fun register(obj: Any?, instance: MokkeryInstance) {
        mapping[obj ?: return] = instance
        reverseMapping[instance] = obj.weaken()
    }
    override fun resolve(obj: Any?): MokkeryInstance? {
        if (obj is MokkeryInstance) return obj
        return mapping[obj ?: return null]
    }

    override fun reverseResolve(obj: MokkeryInstance): Any {
        return reverseMapping[obj]?.value ?: obj
    }
}
