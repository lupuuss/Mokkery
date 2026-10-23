@file:Suppress("unused")

package dev.mokkery.internal

import dev.mokkery.MockMode
import dev.mokkery.MokkeryRuntimeException
import dev.mokkery.MokkeryScope
import dev.mokkery.configurer.MokkeryConfigurer
import dev.mokkery.context.MokkeryContext
import dev.mokkery.internal.context.Settings
import dev.mokkery.internal.context.moduleNameContext
import dev.mokkery.internal.interceptor.forkedHooksOrEmpty
import dev.mokkery.verify.VerifyMode
import kotlinx.atomicfu.atomic
import kotlinx.atomicfu.locks.reentrantLock
import kotlinx.atomicfu.locks.withLock

@PublishedApi
internal fun MokkeryScope.createModuleScope(
    moduleName: String?,
    defaultMockMode: MockMode,
    defaultVerifyMode: VerifyMode,
    initialize: (MokkeryConfigurer) -> Unit,
): MokkeryScope {
    val base = MokkeryContext.memoized {
        +this@createModuleScope.mokkeryContext
        +Settings(defaultMockMode, defaultVerifyMode)
        +moduleNameContext(moduleName)
        +this@createModuleScope.forkedHooksOrEmpty()
    }
    return ModuleScope(base).apply { use(initialize) }
}

@PublishedApi
internal fun runModuleInitializer(name: String, moduleName: String?, initializer: () -> Unit) {
    try {
        initializer()
    } catch (e: Throwable) {
        throw MokkeryRuntimeException("Mokkery initializer function '$name' failed in module '${moduleName ?: "<unknown>"}'.", e)
    }
}

@PublishedApi
internal fun moduleScopeLazy(create: () -> MokkeryScope): Lazy<MokkeryScope> = ModuleScopeLazy(create)

private class ModuleScope(context: MokkeryContext) : MokkeryConfigurer, AutoCloseable {

    private var isClosed = false

    override var mokkeryContext: MokkeryContext = context
        set(value) {
            if (isClosed) mokkeryRuntimeError("Module scope can only be modified in `@MokkeryModuleInit` functions!")
            field = value
        }

    override fun close() {
        isClosed = true
    }

    override fun toString(): String = "MokkeryScope(mokkeryContext=$mokkeryContext)"
}

private class ModuleScopeLazy(private val create: () -> MokkeryScope) : Lazy<MokkeryScope> {

    private val lock = reentrantLock()
    private val published = atomic<MokkeryScope?>(null)
    private var creating = false
    private var failure: Throwable? = null

    override val value: MokkeryScope
        get() = published.value ?: lock.withLock { published.value ?: initialize() }

    override fun isInitialized(): Boolean = published.value != null

    private fun initialize(): MokkeryScope {
        failure?.let { throw MokkeryRuntimeException("`MokkeryScope.module` initialization failed earlier!", it) }
        if (creating) {
            mokkeryRuntimeError(
                "`MokkeryScope.module` is not available while module initializers run! " +
                    "Accept `MokkeryScope` as the initializer parameter instead."
            )
        }
        creating = true
        val scope = try {
            create()
        } catch (e: Throwable) {
            failure = e
            throw e
        } finally {
            creating = false
        }
        published.value = scope
        return scope
    }
}
