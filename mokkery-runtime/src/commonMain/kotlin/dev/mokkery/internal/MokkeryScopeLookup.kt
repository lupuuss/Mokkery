package dev.mokkery.internal

import dev.mokkery.internal.utils.mokkeryRuntimeError

internal interface MokkeryScopeLookup {

    fun registerScope(obj: Any?, scope: MokkeryInstanceScope)

    fun resolveScopeOrNull(obj: Any?): MokkeryInstanceScope?

    fun resolveInstanceOrNull(obj: MokkeryInstanceScope): Any?
}

internal fun MokkeryScopeLookup.resolveScope(obj: Any?): MokkeryInstanceScope {
    return resolveScopeOrNull(obj) ?: throw ObjectNotMockedException(obj)
}

internal fun MokkeryScopeLookup.resolveInstance(scope: MokkeryInstanceScope): Any {
    return resolveInstanceOrNull(scope) ?: mokkeryRuntimeError("Failed to resolve instance associated with scope => $scope")
}

internal expect fun MokkeryScopeLookup(): MokkeryScopeLookup

internal object StaticMokkeryScopeLookup : MokkeryScopeLookup {

    override fun registerScope(obj: Any?, scope: MokkeryInstanceScope): Nothing {
        mokkeryRuntimeError("Registering MokkeryInstance on non-JS platforms is not supported!")
    }

    override fun resolveScopeOrNull(obj: Any?): MokkeryInstanceScope? = obj as? MokkeryInstanceScope

    override fun resolveInstanceOrNull(obj: MokkeryInstanceScope): Any = obj
}
