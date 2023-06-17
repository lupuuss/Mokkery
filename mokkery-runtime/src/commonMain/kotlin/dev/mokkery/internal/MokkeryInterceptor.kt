package dev.mokkery.internal

import dev.mokkery.internal.answering.autofillValue
import kotlin.reflect.KClass


internal interface MokkeryInterceptor {

    fun interceptCall(signature: String, returnType: KClass<*>, varArgPosition: Int, vararg args: Any?): Any?

    suspend fun interceptSuspendCall(
        signature: String,
        returnType: KClass<*>,
        varArgPosition: Int,
        vararg args: Any?
    ): Any? = interceptCall(signature, returnType, varArgPosition, *args)

}

internal enum class MokkeryToken {
    CALL_NEXT, RETURN_DEFAULT
}

internal fun combine(vararg interceptors: MokkeryInterceptor): MokkeryInterceptor {
    return CombinedInterceptor(interceptors)
}

private class CombinedInterceptor(private val interceptors: Array<out MokkeryInterceptor>) : MokkeryInterceptor {
    override fun interceptCall(signature: String, returnType: KClass<*>, varArgPosition: Int, vararg args: Any?): Any? {
        interceptors.forEach {
            when (val result = it.interceptCall(signature, returnType, varArgPosition, *args)) {
                MokkeryToken.CALL_NEXT -> Unit
                MokkeryToken.RETURN_DEFAULT -> return autofillValue(returnType)
                else -> return result
            }
        }
        return autofillValue(returnType)
    }

    override suspend fun interceptSuspendCall(
        signature: String,
        returnType: KClass<*>,
        varArgPosition: Int,
        vararg args: Any?
    ): Any? {
        interceptors.forEach {
            when (val result = it.interceptSuspendCall(signature, returnType, varArgPosition, *args)) {
                MokkeryToken.CALL_NEXT -> Unit
                MokkeryToken.RETURN_DEFAULT -> return autofillValue(returnType)
                else -> return result
            }
        }
        return autofillValue(returnType)
    }

}
