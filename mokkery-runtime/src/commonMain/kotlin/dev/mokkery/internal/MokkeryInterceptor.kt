package dev.mokkery.internal

import dev.mokkery.answering.autofill.AutofillProvider
import dev.mokkery.answering.autofill.provideValue
import kotlin.reflect.KClass


internal interface MokkeryInterceptor {

    fun interceptCall(context: CallContext): Any?

    suspend fun interceptSuspendCall(context: CallContext): Any? = interceptCall(context)

}

internal sealed interface MokkeryToken {
    data object CallNext : MokkeryToken
    data class ReturnDefault(val genericReturnTypeHint: KClass<*>?) : MokkeryToken
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
                MokkeryToken.CallNext -> Unit
                is MokkeryToken.ReturnDefault -> return autofillWithGenericHint(result, context)
                else -> return result
            }
        }
        return autofill.provideValue(context.returnType)
    }

    override suspend fun interceptSuspendCall(context: CallContext): Any? {
        interceptors.forEach {
            when (val result = it.interceptSuspendCall(context)) {
                MokkeryToken.CallNext -> Unit
                is MokkeryToken.ReturnDefault -> return autofillWithGenericHint(result, context)
                else -> return result
            }
        }
        return autofill.provideValue(context.returnType)
    }

    private fun autofillWithGenericHint(token: MokkeryToken.ReturnDefault, context: CallContext): Any? {
        return autofill.provideValue(token.genericReturnTypeHint ?: context.returnType)
    }
}
