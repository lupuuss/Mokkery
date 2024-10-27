package dev.mokkery.internal.interceptor

import dev.mokkery.answering.autofill.provideValue
import dev.mokkery.internal.context.autofillProvider
import dev.mokkery.context.call
import dev.mokkery.interceptor.MokkeryBlockingCallScope
import dev.mokkery.interceptor.MokkeryCallInterceptor
import dev.mokkery.interceptor.MokkeryCallScope
import dev.mokkery.interceptor.MokkerySuspendCallScope
import dev.mokkery.interceptor.nextIntercept
import dev.mokkery.internal.ConcurrentTemplatingException
import dev.mokkery.internal.calls.TemplatingScope
import dev.mokkery.internal.context.currentMokkeryInstance
import dev.mokkery.internal.id
import kotlinx.atomicfu.atomic

internal interface TemplatingInterceptor : MokkeryCallInterceptor {

    val isEnabled: Boolean

    fun isEnabledWith(scope: TemplatingScope): Boolean

    fun start(scope: TemplatingScope)

    fun stop()
}

internal fun TemplatingInterceptor(): TemplatingInterceptor = TemplatingInterceptorImpl()

private class TemplatingInterceptorImpl : TemplatingInterceptor {

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

    override fun intercept(scope: MokkeryBlockingCallScope) = intercept(scope) { scope.nextIntercept() }

    override suspend fun intercept(scope: MokkerySuspendCallScope) = intercept(scope) { scope.nextIntercept() }

    private inline fun intercept(scope: MokkeryCallScope, nextIntercept: () -> Any?): Any? {
        if (!_isEnabled) return nextIntercept()
        val hint = templatingScope?.currentGenericReturnTypeHint
        val context = scope.context
        val call = context.call
        templatingScope?.saveTemplate(context.currentMokkeryInstance.id, call.function.name, call.args)
            ?: throw ConcurrentTemplatingException()
        return context.autofillProvider.provideValue(hint ?: call.function.returnType)
    }
}
