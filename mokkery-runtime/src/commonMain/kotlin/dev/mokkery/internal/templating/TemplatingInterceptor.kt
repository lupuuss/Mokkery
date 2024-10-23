package dev.mokkery.internal.templating

import dev.mokkery.internal.CallContext
import dev.mokkery.internal.ConcurrentTemplatingException
import dev.mokkery.internal.MokkeryInterceptor
import dev.mokkery.internal.MokkeryToken
import dev.mokkery.internal.id
import kotlinx.atomicfu.atomic

internal interface TemplatingInterceptor : MokkeryInterceptor {

    val isEnabled: Boolean

    fun isEnabledWith(scope: TemplatingScope): Boolean

    fun start(scope: TemplatingScope)

    fun stop()
}

internal fun TemplatingInterceptor(): TemplatingInterceptor = TemplatingMokkeryInterceptorImpl()

private class TemplatingMokkeryInterceptorImpl : TemplatingInterceptor {

    private var _isEnabled by atomic(false)
    private var templatingScope by atomic<TemplatingScope?>(null)
    override val isEnabled: Boolean get() = _isEnabled
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

    override fun interceptCall(context: CallContext): Any {
        if (!_isEnabled) {
            return MokkeryToken.CallNext
        }
        val hint = templatingScope?.currentGenericReturnTypeHint
        templatingScope?.saveTemplate(context.instance.id, context.name, context.args)
            ?: throw ConcurrentTemplatingException()
        return MokkeryToken.ReturnDefault(hint)
    }

}
