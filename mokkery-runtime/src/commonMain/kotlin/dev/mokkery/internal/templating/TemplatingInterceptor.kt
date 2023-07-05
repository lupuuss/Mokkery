package dev.mokkery.internal.templating

import dev.mokkery.internal.ConcurrentTemplatingException
import dev.mokkery.internal.MokkeryToken
import dev.mokkery.internal.MokkeryInterceptor
import dev.mokkery.internal.tracing.CallArg
import kotlinx.atomicfu.atomic
import kotlin.reflect.KClass

internal interface TemplatingInterceptor : MokkeryInterceptor {

    val isEnabled: Boolean

    fun isEnabledWith(scope: TemplatingScope): Boolean

    fun start(scope: TemplatingScope)

    fun stop()
}

internal fun TemplatingInterceptor(receiver: String): TemplatingInterceptor {
    return TemplatingMokkeryInterceptorImpl(receiver)
}

private class TemplatingMokkeryInterceptorImpl(private val receiver: String) : TemplatingInterceptor {

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

    override fun interceptCall(name: String, returnType: KClass<*>, vararg args: CallArg): Any {
        if (!_isEnabled) {
            return MokkeryToken.CALL_NEXT
        }
        templatingScope?.saveTemplate(receiver, name, args.toList())
            ?: throw ConcurrentTemplatingException()
        return MokkeryToken.RETURN_DEFAULT
    }

}
