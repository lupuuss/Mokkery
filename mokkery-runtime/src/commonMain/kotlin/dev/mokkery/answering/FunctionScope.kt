package dev.mokkery.answering

import dev.mokkery.internal.unsafeCast
import kotlin.reflect.KClass

/**
 * Provides function call arguments.
 */
public class FunctionScope(
    public val returnType: KClass<*>,
    public val args: List<Any?>
) {

    override fun toString(): String = args.toString()

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        other as FunctionScope

        return args == other.args
    }

    override fun hashCode(): Int {
        return args.hashCode()
    }

    public operator fun <T> component1(): T = args[0].unsafeCast()
    public operator fun <T> component2(): T = args[1].unsafeCast()
    public operator fun <T> component3(): T = args[2].unsafeCast()
    public operator fun <T> component4(): T = args[3].unsafeCast()
    public operator fun <T> component5(): T = args[4].unsafeCast()
    public operator fun <T> component6(): T = args[5].unsafeCast()
    public operator fun <T> component7(): T = args[6].unsafeCast()
}
