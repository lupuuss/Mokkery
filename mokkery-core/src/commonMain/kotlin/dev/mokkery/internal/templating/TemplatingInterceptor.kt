package dev.mokkery.internal.templating

import dev.mokkery.internal.ConcurrentTemplatingException
import dev.mokkery.internal.MokkeryToken
import dev.mokkery.internal.MokkeryInterceptor
import kotlinx.atomicfu.atomic
import kotlin.reflect.KClass

internal interface TemplatingInterceptor : MokkeryInterceptor {

    fun start(context: TemplatingContext)

    fun stop()
}

internal fun TemplatingInterceptor(receiver: String): TemplatingInterceptor {
    return TemplatingMokkeryInterceptorImpl(receiver)
}

private class TemplatingMokkeryInterceptorImpl(private val receiver: String) : TemplatingInterceptor {

    private var isEnabled by atomic(false)
    private var templatingContext by atomic<TemplatingContext?>(null)

    override fun start(context: TemplatingContext) {
        if (isEnabled) throw ConcurrentTemplatingException()
        isEnabled = true
        templatingContext = context
    }

    override fun stop() {
        if (!isEnabled) throw ConcurrentTemplatingException()
        isEnabled = false
        templatingContext = null
    }

    override fun interceptCall(signature: String, returnType: KClass<*>, vararg args: Any?): Any? {
        if (!isEnabled) {
            return MokkeryToken.CALL_NEXT
        }
        templatingContext?.saveTemplate(receiver, signature, args) ?: throw ConcurrentTemplatingException()
        return MokkeryToken.RETURN_DEFAULT
    }

}
