package dev.mokkery.internal

import dev.mokkery.internal.answering.autofillValue
import dev.mokkery.internal.tracing.CallArg
import kotlin.reflect.KClass


internal interface MokkeryInterceptor {

    fun interceptCall(name: String, returnType: KClass<*>, vararg args: CallArg): Any?

    suspend fun interceptSuspendCall(
        name: String,
        returnType: KClass<*>,
        vararg args: CallArg
    ): Any? = interceptCall(name, returnType, *args)

}

internal enum class MokkeryToken {
    CALL_NEXT, RETURN_DEFAULT
}

internal fun combine(vararg interceptors: MokkeryInterceptor): MokkeryInterceptor {
    return CombinedInterceptor(interceptors)
}

private class CombinedInterceptor(private val interceptors: Array<out MokkeryInterceptor>) : MokkeryInterceptor {
    override fun interceptCall(name: String, returnType: KClass<*>, vararg args: CallArg): Any? {
        interceptors.forEach {
            when (val result = it.interceptCall(name, returnType, *args)) {
                MokkeryToken.CALL_NEXT -> Unit
                MokkeryToken.RETURN_DEFAULT -> return autofillValue(returnType)
                else -> return result
            }
        }
        return autofillValue(returnType)
    }

    override suspend fun interceptSuspendCall(
        name: String,
        returnType: KClass<*>,
        vararg args: CallArg
    ): Any? {
        interceptors.forEach {
            when (val result = it.interceptSuspendCall(name, returnType, *args)) {
                MokkeryToken.CALL_NEXT -> Unit
                MokkeryToken.RETURN_DEFAULT -> return autofillValue(returnType)
                else -> return result
            }
        }
        return autofillValue(returnType)
    }

}
