package dev.mokkery.internal.context

import dev.mokkery.MokkeryScope
import dev.mokkery.context.MokkeryContext

internal val MokkeryScope.moduleName: String?
    get() = mokkeryContext[ModuleName]?.value

internal fun moduleNameContext(moduleName: String?): MokkeryContext {
    moduleName ?: return MokkeryContext.Empty
    return ModuleName(moduleName)
}

private data class ModuleName(val value: String) : MokkeryContext.Element {

    override val key: Key get() = Key

    companion object Key : MokkeryContext.Key<ModuleName>
}
