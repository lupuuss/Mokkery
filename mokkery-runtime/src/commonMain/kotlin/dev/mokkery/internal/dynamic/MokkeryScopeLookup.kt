package dev.mokkery.internal.dynamic

import dev.mokkery.internal.MokkeryInterceptorScope

internal interface MokkeryScopeLookup {
    fun register(obj: Any?, scope: MokkeryInterceptorScope)

    fun resolve(obj: Any?): MokkeryInterceptorScope?

    companion object {

        val current = MokkeryScopeLookup()
    }
}

internal expect fun MokkeryScopeLookup(): MokkeryScopeLookup
