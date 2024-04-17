package dev.mokkery.answering

import dev.mokkery.annotations.DelicateMokkeryApi
import dev.mokkery.internal.MissingArgsForSuperMethodException
import dev.mokkery.internal.MissingSuperMethodException
import dev.mokkery.internal.ObjectNotMockedException
import dev.mokkery.internal.dynamic.MokkeryScopeLookup
import dev.mokkery.internal.unsafeCastOrNull
import kotlin.reflect.KClass


/**
 * Provides a set of mocked function related operations that might be required for implementing [Answer].
 */
@DelicateMokkeryApi
public class FunctionScope internal constructor(
    /**
     * Return type of mocked method.
     */
    public val returnType: KClass<*>,
    /**
     * Args passed to mocked method. If method has extension receiver it is passed at the start of this list.
     */
    public val args: List<Any?>,
    /**
     * Reference to this mock.
     */
    public val self: Any?,
    /**
     * Contains super method for given super type.
     * This map might contain suspend functions that require cast.
     *
     * Use [callSuper], [callSuspendSuper], [callOriginal], [callSuspendOriginal] for convenience.
     */
    public val supers: Map<KClass<*>, (args: List<Any?>) -> Any?>
) {

    /**
     * Returns argument with [index] from [args] and expects that it is an instance of type [T].
     */
    public inline fun <reified T> arg(index: Int): T = args[index] as T

    override fun toString(): String = "FunctionScope(self=$self, returnType=$returnType, args=$args, supers=$supers)"

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        other as FunctionScope

        if (returnType != other.returnType) return false
        if (args != other.args) return false
        if (self != other.self) return false
        if (supers != other.supers) return false

        return true
    }

    override fun hashCode(): Int {
        var result = returnType.hashCode()
        result = 31 * result + args.hashCode()
        result = 31 * result + (self?.hashCode() ?: 0)
        result = 31 * result + supers.hashCode()
        return result
    }

    /**
     * Calls super method of [superType] with given [args]
     */
    public fun callSuper(superType: KClass<*>, args: List<Any?>): Any? {
        if (this.args.size != args.size) {
            throw MissingArgsForSuperMethodException(this.args.size, args.size)
        }
        return supers[superType]
            .let { it ?: throw MissingSuperMethodException(superType) }
            .invoke(args)
    }

    /**
     * Just like [callSuper] but for suspend calls.
     */
    public suspend fun callSuspendSuper(superType: KClass<*>, args: List<Any?>): Any? {
        if (this.args.size != args.size) {
            throw MissingArgsForSuperMethodException(this.args.size, args.size)
        }
        return supers[superType]
            .unsafeCastOrNull<suspend (List<Any?>) -> Any?>()
            .let { it ?: throw MissingSuperMethodException(superType) }
            .invoke(args)
    }

    /**
     * Calls original method implementation with given [args].
     */
    public fun callOriginal(args: List<Any?>): Any? = callOriginal(MokkeryScopeLookup.current, args)

    /**
     * Just like [callOriginal] but for suspend calls.
     */
    public suspend fun callSuspendOriginal(args: List<Any?>): Any? = callSuspendOriginal(MokkeryScopeLookup.current, args)

    internal fun callOriginal(lookup: MokkeryScopeLookup, args: List<Any?>): Any? {
        if (this.args.size != args.size) {
            throw MissingArgsForSuperMethodException(this.args.size, args.size)
        }
        val superType = lookup.resolve(self)?.interceptedType ?: throw ObjectNotMockedException(self)
        return supers[superType]
            .let { it ?: throw MissingSuperMethodException(superType) }
            .invoke(args)
    }

    internal suspend fun callSuspendOriginal(lookup: MokkeryScopeLookup, args: List<Any?>): Any? {
        if (this.args.size != args.size) {
            throw MissingArgsForSuperMethodException(this.args.size, args.size)
        }
        val superType = lookup.resolve(self)?.interceptedType ?: throw ObjectNotMockedException(self)
        return supers[superType]
            .unsafeCastOrNull<suspend (List<Any?>) -> Any?>()
            .let { it ?: throw MissingSuperMethodException(superType) }
            .invoke(args)
    }
}
