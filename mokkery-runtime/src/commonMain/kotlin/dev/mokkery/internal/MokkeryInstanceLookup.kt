package dev.mokkery.internal

import dev.mokkery.internal.utils.mokkeryRuntimeError

internal interface MokkeryInstanceLookup {

    fun register(obj: Any?, instance: MokkeryInstance)

    fun resolve(obj: Any?): MokkeryInstance?

    fun reverseResolve(obj: MokkeryInstance): Any?
}

internal expect fun MokkeryInstanceLookup(): MokkeryInstanceLookup

internal object StaticMokkeryInstanceLookup : MokkeryInstanceLookup {

    override fun register(obj: Any?, instance: MokkeryInstance): Nothing {
        mokkeryRuntimeError("Registering MokkeryInstance on non-JS platforms is not supported!")
    }

    override fun resolve(obj: Any?): MokkeryInstance? = obj as? MokkeryInstance

    override fun reverseResolve(obj: MokkeryInstance): Any = obj
}
