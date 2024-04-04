package dev.mokkery.internal

import dev.mokkery.answering.autofill.AutofillProvider
import dev.mokkery.answering.autofill.provideValue


internal interface MokkeryInterceptor {

    fun interceptCall(context: CallContext): Any?

    suspend fun interceptSuspendCall(context: CallContext): Any? = interceptCall(context)

}

internal enum class MokkeryToken {
    CALL_NEXT, RETURN_DEFAULT
}

internal fun combine(vararg interceptors: MokkeryInterceptor): MokkeryInterceptor {
    return CombinedInterceptor(interceptors)
}

private class CombinedInterceptor(
    private val interceptors: Array<out MokkeryInterceptor>,
    private val autofill: AutofillProvider<Any?> = AutofillProvider.forInternals,
) : MokkeryInterceptor {

    override fun interceptCall(context: CallContext): Any? {
        interceptors.forEach {
            when (val result = it.interceptCall(context)) {
                MokkeryToken.CALL_NEXT -> Unit
                MokkeryToken.RETURN_DEFAULT -> return autofill.provideValue(context.returnType)
                else -> return result
            }
        }
        return autofill.provideValue(context.returnType)
    }

    override suspend fun interceptSuspendCall(context: CallContext): Any? {
        interceptors.forEach {
            when (val result = it.interceptSuspendCall(context)) {
                MokkeryToken.CALL_NEXT -> Unit
                MokkeryToken.RETURN_DEFAULT -> return autofill.provideValue(context.returnType)
                else -> return result
            }
        }
        return autofill.provideValue(context.returnType)
    }

}
