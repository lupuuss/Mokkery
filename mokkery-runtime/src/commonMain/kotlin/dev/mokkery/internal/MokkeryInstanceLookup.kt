package dev.mokkery.internal

import dev.mokkery.internal.utils.mokkeryRuntimeError

internal interface MokkeryInstanceLookup {

    fun register(obj: Any?, instance: MokkeryInstanceScope)

    fun resolve(obj: Any?): MokkeryInstanceScope?

    fun reverseResolve(obj: MokkeryInstanceScope): Any?
}

internal expect fun MokkeryInstanceLookup(): MokkeryInstanceLookup

internal object StaticMokkeryInstanceLookup : MokkeryInstanceLookup {

    override fun register(obj: Any?, instance: MokkeryInstanceScope): Nothing {
        mokkeryRuntimeError("Registering MokkeryInstance on non-JS platforms is not supported!")
    }

    override fun resolve(obj: Any?): MokkeryInstanceScope? = obj as? MokkeryInstanceScope

    override fun reverseResolve(obj: MokkeryInstanceScope): Any = obj
}
