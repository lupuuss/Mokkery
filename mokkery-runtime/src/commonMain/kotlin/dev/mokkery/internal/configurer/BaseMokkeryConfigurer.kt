package dev.mokkery.internal.configurer

import dev.mokkery.configurer.MokkeryConfigurer
import dev.mokkery.context.MokkeryContext
import dev.mokkery.internal.mokkeryRuntimeError

internal abstract class ClosableMokkeryConfigurer : MokkeryConfigurer, AutoCloseable {

    private var isClosed = false

    protected inline fun <T> ensureOpen(block: () -> T): T {
        if (isClosed) mokkeryRuntimeError("Configurer is closed.")
        return block()
    }

    override fun close() {
        isClosed = true
    }
}

internal open class BaseMokkeryConfigurer(
    context: MokkeryContext,
) : ClosableMokkeryConfigurer() {

    override var mokkeryContext: MokkeryContext = context
        get() = ensureOpen { field }
        set(value) = ensureOpen { field = value }
}

internal fun <T : BaseMokkeryConfigurer> MokkeryContext.applyConfigurer(
    configurer: (MokkeryContext) -> T,
    block: T.() -> Unit,
): MokkeryContext = configurer(this).use { it.apply(block).mokkeryContext }
