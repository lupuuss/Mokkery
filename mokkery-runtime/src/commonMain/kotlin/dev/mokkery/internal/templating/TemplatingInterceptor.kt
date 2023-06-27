package dev.mokkery.internal.templating

import dev.mokkery.internal.ConcurrentTemplatingException
import dev.mokkery.internal.MokkeryToken
import dev.mokkery.internal.MokkeryInterceptor
import dev.mokkery.internal.tracing.CallArg
import kotlinx.atomicfu.atomic
import kotlin.reflect.KClass

internal interface TemplatingInterceptor : MokkeryInterceptor {

    val isEnabled: Boolean

    fun start(context: TemplatingContext)

    fun stop()
}

internal fun TemplatingInterceptor(receiver: String): TemplatingInterceptor {
    return TemplatingMokkeryInterceptorImpl(receiver)
}

private class TemplatingMokkeryInterceptorImpl(private val receiver: String) : TemplatingInterceptor {

    private var _isEnabled by atomic(false)
    private var templatingContext by atomic<TemplatingContext?>(null)
    override val isEnabled: Boolean get() = _isEnabled

    override fun start(context: TemplatingContext) {
        if (_isEnabled) throw ConcurrentTemplatingException()
        _isEnabled = true
        templatingContext = context
    }

    override fun stop() {
        if (!_isEnabled) throw ConcurrentTemplatingException()
        _isEnabled = false
        templatingContext = null
    }

    override fun interceptCall(name: String, returnType: KClass<*>, vararg args: CallArg): Any? {
        if (!_isEnabled) {
            return MokkeryToken.CALL_NEXT
        }
        templatingContext?.saveTemplate(receiver, name, args)
            ?: throw ConcurrentTemplatingException()
        return MokkeryToken.RETURN_DEFAULT
    }

}
