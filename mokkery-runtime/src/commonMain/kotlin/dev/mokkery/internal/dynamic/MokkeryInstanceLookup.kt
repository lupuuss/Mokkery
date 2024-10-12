package dev.mokkery.internal.dynamic

import dev.mokkery.internal.MokkeryInstance
import dev.mokkery.internal.mokkeryRuntimeError

internal interface MokkeryInstanceLookup {
    fun register(obj: Any?, instance: MokkeryInstance)

    fun resolve(obj: Any?): MokkeryInstance?

    fun reverseResolve(obj: MokkeryInstance): Any?

    companion object {

        val current = MokkeryInstanceLookup()
    }
}

internal expect fun MokkeryInstanceLookup(): MokkeryInstanceLookup

internal object StaticMokkeryInstanceLookup : MokkeryInstanceLookup {
    override fun register(obj: Any?, instance: MokkeryInstance): Nothing {
        mokkeryRuntimeError("Registering MokkeryInterceptorScope on non-JS platforms is not supported!")
    }
    override fun resolve(obj: Any?): MokkeryInstance? = obj as? MokkeryInstance
    override fun reverseResolve(obj: MokkeryInstance): Any = obj
}
