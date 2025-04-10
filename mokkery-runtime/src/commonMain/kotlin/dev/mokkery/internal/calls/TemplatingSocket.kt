package dev.mokkery.internal.calls

import dev.mokkery.MokkeryScope
import dev.mokkery.context.MokkeryContext
import dev.mokkery.context.require
import dev.mokkery.interceptor.MokkeryCallScope
import dev.mokkery.interceptor.call
import dev.mokkery.internal.ConcurrentTemplatingException
import dev.mokkery.internal.context.mockSpec
import kotlinx.atomicfu.atomic
import kotlin.reflect.KClass

internal interface TemplatingSocket : MokkeryContext.Element {

    override val key: MokkeryContext.Key<*> get() = Key
    
    val isEnabled: Boolean

    val currentGenericHint: KClass<*>?

    fun isEnabledWith(scope: TemplatingScope): Boolean

    fun start(scope: TemplatingScope)

    fun stop()

    fun saveTemplate(scope: MokkeryCallScope)
    
    companion object Key : MokkeryContext.Key<TemplatingSocket>
}

internal val MokkeryScope.templating: TemplatingSocket
    get() = mokkeryContext.require(TemplatingSocket)

internal fun TemplatingSocket(): TemplatingSocket = TemplatingSocketImpl()

private class TemplatingSocketImpl : TemplatingSocket {
    
    private var _isEnabled by atomic(false)
    private var templatingScope by atomic<TemplatingScope?>(null)
    override val isEnabled: Boolean get() = _isEnabled

    override val currentGenericHint: KClass<*>?
        get() = templatingScope?.currentGenericReturnTypeHint

    override fun isEnabledWith(scope: TemplatingScope): Boolean = templatingScope == scope

    override fun start(scope: TemplatingScope) {
        if (_isEnabled) throw ConcurrentTemplatingException()
        _isEnabled = true
        templatingScope = scope
    }

    override fun stop() {
        if (!_isEnabled) throw ConcurrentTemplatingException()
        _isEnabled = false
        templatingScope = null
    }

    override fun saveTemplate(scope: MokkeryCallScope) {
        val call = scope.call
        templatingScope
            ?.saveTemplate(scope.mockSpec.id, call.function.name, call.args)
            ?: throw ConcurrentTemplatingException()
    }

    override fun toString(): String = "TemplatingSocket@${hashCode()}"
}
