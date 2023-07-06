package dev.mokkery.answering

import dev.mokkery.annotations.DelicateMokkeryApi
import dev.mokkery.internal.SuspendingFunctionBlockingCallException
import dev.mokkery.internal.answering.autofillValue
import kotlin.reflect.KClass

@DelicateMokkeryApi
public interface Answer<out T> {

    public fun call(returnType: KClass<*>, args: List<Any?>): T

    public suspend fun callSuspend(returnType: KClass<*>, args: List<Any?>): T = call(returnType, args)

    public class Const<T>(private val value: T) : Answer<T> {
        override fun call(returnType: KClass<*>, args: List<Any?>): T = value
    }

    public class Block<T>(private val block: (CallArgs) -> T) : Answer<T> {
        override fun call(returnType: KClass<*>, args: List<Any?>): T = block(CallArgs(args))
    }

    public class Throws(private val throwable: Throwable) : Answer<Nothing> {
        override fun call(returnType: KClass<*>, args: List<Any?>): Nothing = throw throwable
    }

    public class BlockSuspend<T>(private val block: suspend (CallArgs) -> T) : Answer<T> {
        override fun call(returnType: KClass<*>, args: List<Any?>): T = throw SuspendingFunctionBlockingCallException()

        override suspend fun callSuspend(returnType: KClass<*>, args: List<Any?>): T {
            return block(CallArgs(args))
        }
    }

    public object Autofill : Answer<Any?> {
        override fun call(returnType: KClass<*>, args: List<Any?>): Any? = autofillValue(returnType)
    }

}
