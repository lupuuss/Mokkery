package dev.mokkery.answering

import dev.mokkery.internal.unsafeCast
import kotlin.reflect.KClass

/**
 * Provides function call arguments. If function has any extension receiver, it is provided at the beginning of the [args] list.
 */
public class FunctionScope(
    public val returnType: KClass<*>,
    public val args: List<Any?>,
    public val self: Any?,
) {
    public operator fun <T> component1(): T = args[0].unsafeCast()
    public operator fun <T> component2(): T = args[1].unsafeCast()
    public operator fun <T> component3(): T = args[2].unsafeCast()
    public operator fun <T> component4(): T = args[3].unsafeCast()
    public operator fun <T> component5(): T = args[4].unsafeCast()
    public operator fun <T> component6(): T = args[5].unsafeCast()
    public operator fun <T> component7(): T = args[6].unsafeCast()

    public inline fun <reified T> self(): T = self as T

    override fun toString(): String = "FunctionScope(self=$self, returnType=$returnType, args=$args)"

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        other as FunctionScope

        if (returnType != other.returnType) return false
        if (args != other.args) return false
        return self == other.self
    }

    override fun hashCode(): Int {
        var result = returnType.hashCode()
        result = 31 * result + args.hashCode()
        result = 31 * result + (self?.hashCode() ?: 0)
        return result
    }
}
