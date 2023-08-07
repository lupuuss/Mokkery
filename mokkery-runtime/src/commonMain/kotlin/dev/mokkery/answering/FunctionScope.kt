package dev.mokkery.answering

import dev.mokkery.annotations.DelicateMokkeryApi
import dev.mokkery.internal.MissingArgsForSuperMethodException
import dev.mokkery.internal.MissingSuperMethodException
import dev.mokkery.internal.ObjectNotMockedException
import dev.mokkery.internal.dynamic.MokkeryScopeLookup
import dev.mokkery.internal.unsafeCast
import kotlin.reflect.KClass

/**
 * Provides function call arguments. If function has any extension receiver, it is provided at the beginning of the [args] list.
 */
public class FunctionScope internal constructor(
    /**
     * Return type of mocked method.
     */
    public val returnType: KClass<*>,
    /**
     * Args passed to mocked method.
     */
    public val args: List<Any?>,
    /**
     * Reference to this mock.
     */
    public val self: Any?,
    /**
     * Contains super method for given super type.
     * Use [callSuper] and [callSuperWith] for convenience.
     */
    @DelicateMokkeryApi
    public val supers: Map<KClass<*>, (args: List<Any?>) -> Any?>
) {

    public inline operator fun <reified T> component1(): T = arg(0)
    public inline operator fun <reified T> component2(): T = arg(1)
    public inline operator fun <reified T> component3(): T = arg(2)
    public inline operator fun <reified T> component4(): T = arg(3)
    public inline operator fun <reified T> component5(): T = arg(4)
    public inline operator fun <reified T> component6(): T = arg(5)
    public inline operator fun <reified T> component7(): T = arg(6)

    /**
     * Returns argument with [index] from [args] and expects that it is an instance of type [T].
     */
    public inline fun <reified T> arg(index: Int): T = args[index] as T

    /**
     * Returns [self] as [T].
     */
    public inline fun <reified T> self(): T = self as T

    /**
     * Calls method from super type [T] with given [args]. This method expects that super method returns [R].
     */
    public inline fun <reified T, reified R> callSuperWith(vararg args: Any?): R = callSuper(T::class, args.toList()) as R

    /**
     * Calls method from super type [T] with [FunctionScope.args]. This method expects that super method returns [R].
     */
    public inline fun <reified T, reified R> callSuper(): R = callSuper(T::class, args) as R

    /**
     * Calls original method of mocked type. Shorthand for `callSuper<MockedType, _>()`.
     */
    public fun <R> callOriginal(): R = callOriginal(MokkeryScopeLookup.current, args).unsafeCast()

    /**
     * Calls original method of mocked type with given [args]. Shorthand for `callOriginalWith<MockedType, _>()`.
     */
    public fun <R> callOriginalWith(vararg args: Any?): R = callOriginal(MokkeryScopeLookup.current, args.toList())
        .unsafeCast()

    override fun toString(): String = "FunctionScope(self=$self, returnType=$returnType, args=$args)"

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

    @PublishedApi
    internal fun callSuper(superType: KClass<*>, args: List<Any?>): Any? {
        if (this.args.size != args.size) {
            throw MissingArgsForSuperMethodException(this.args.size, args.size)
        }
        return supers[superType]
            .let { it ?: throw MissingSuperMethodException(superType) }
            .invoke(args)
    }

    internal fun callOriginal(lookup: MokkeryScopeLookup, args: List<Any?>): Any? {
        if (this.args.size != args.size) {
            throw MissingArgsForSuperMethodException(this.args.size, args.size)
        }
        val superType = lookup.resolve(self)?.interceptedType ?: throw ObjectNotMockedException(self)
        return supers[superType]
            .let { it ?: throw MissingSuperMethodException(superType) }
            .invoke(args)
    }
}
