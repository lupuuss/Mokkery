package dev.mokkery.internal.configurer

import dev.mokkery.configurer.MokkeryConfigurer
import dev.mokkery.context.MokkeryContext
import dev.mokkery.internal.mokkeryRuntimeError

internal open class BaseMokkeryConfigurer(
    context: MokkeryContext,
) : MokkeryConfigurer, AutoCloseable {

    private var isClosed = false

    override var mokkeryContext: MokkeryContext = context
        get() = ensureOpen { field }
        set(value) = ensureOpen { field = value }


    override fun close() {
        isClosed = true
    }

    private inline fun <T> ensureOpen(block: () -> T): T {
        if (isClosed) mokkeryRuntimeError("Configurer is closed.")
        return block()
    }
}
