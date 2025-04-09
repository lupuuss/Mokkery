package dev.mokkery.internal

import dev.mokkery.MokkeryScope
import dev.mokkery.internal.context.tools
import dev.mokkery.internal.utils.mokkeryRuntimeError

internal interface MokkeryScopeLookup {

    fun registerScope(obj: Any?, scope: MokkeryInstanceScope)

    fun resolveScopeOrNull(obj: Any?): MokkeryInstanceScope?

    fun resolveInstanceOrNull(obj: MokkeryInstanceScope): Any?
}

internal fun MokkeryScope.resolveScope(obj: Any?) = tools.scopeLookup.resolveScope(obj)

internal fun MokkeryScope.resolveScopeOrNull(obj: Any?) = tools.scopeLookup.resolveScopeOrNull(obj)

internal fun MokkeryScope.resolveInstance(instance: MokkeryInstanceScope) = tools
    .scopeLookup
    .resolveInstance(instance)

internal fun MokkeryScopeLookup.resolveScope(obj: Any?): MokkeryInstanceScope = resolveScopeOrNull(obj) ?: throw ObjectNotMockedException(obj)

internal fun MokkeryScopeLookup.resolveInstance(scope: MokkeryInstanceScope): Any {
    return resolveInstanceOrNull(scope) ?: mokkeryRuntimeError("Failed to resolve instance associated with scope => $scope")
}

internal expect fun MokkeryScopeLookup(): MokkeryScopeLookup

internal object StaticMokkeryScopeLookup : MokkeryScopeLookup {

    override fun registerScope(obj: Any?, scope: MokkeryInstanceScope) = Unit

    override fun resolveScopeOrNull(obj: Any?): MokkeryInstanceScope? = obj as? MokkeryInstanceScope

    override fun resolveInstanceOrNull(obj: MokkeryInstanceScope): Any = obj
}
