package dev.mokkery.answering

import dev.mokkery.annotations.DelicateMokkeryApi
import dev.mokkery.internal.answering.SuperCallAnswer
import kotlin.reflect.KClass

/**
 * Describes a super call that should be performed as an answer for a mocked method call.
 */
public sealed interface SuperCall {

    /**
     * Call for a super method from [type] with given [args]. If interface is mocked, default implementation is called.
     *
     * If [args] is null, args passed to mock ([FunctionScope.args]) are used.
     *
     * It is recommended to use [superOf] and [superWith] instead.
     */
    @DelicateMokkeryApi
    public class OfType(public val type: KClass<*>, public val args: List<Any?>? = null) : SuperCall {

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other == null || this::class != other::class) return false
            other as OfType
            if (type != other.type) return false
            if (args != other.args) return false
            return true
        }

        override fun hashCode(): Int {
            var result = type.hashCode()
            result = 31 * result + (args?.hashCode() ?: 0)
            return result
        }

        override fun toString(): String = "OfType(type=$type, args=$args)"
    }

    /**
     * Call for an original method of mocked type. If interface is mocked, default implementation is called.
     *
     * If [args] is null, args passed to mock ([FunctionScope.args]) are used.
     *
     * It is recommended to use [original] and [originalWith] instead.
     */
    @DelicateMokkeryApi
    public class Original(public val args: List<Any?>? = null) : SuperCall {

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other == null || this::class != other::class) return false
            other as Original
            return args == other.args
        }

        override fun hashCode(): Int = args?.hashCode() ?: 0

        override fun toString(): String = "Original(args=$args)"
    }

    public companion object {

        /**
         * Results in a super method call from type [T] with args passed to mock ([FunctionScope.args]).
         * If interface is mocked, default implementation is called.
         */
        public inline fun <reified T> superOf(): SuperCall = OfType(T::class)

        /**
         * Results in a super method call from type [T] with given [args].
         * If interface is mocked, default implementation is called.
         */
        public inline fun <reified T> superWith(vararg args: Any?): SuperCall = OfType(T::class, args.toList())

        /**
         * Results in an original method call from mocked type with args passed to mock ([FunctionScope.args]).
         * If interface is mocked, default implementation is called.
         *
         * It is shorthand for `superOf<MockedType>()`.
         */
        public val original: SuperCall get() = Original()

        /**
         * Call for an original method from mocked type with given [args].
         * If interface is mocked, default implementation is called.
         *
         * It is shorthand for `superWith<MockedType>(...)`.
         */
        public fun originalWith(vararg args: Any?): SuperCall = Original(args.toList())
    }
}

/**
 * Calls super method according to [SuperCall].
 * @see SuperCall.original
 * @see SuperCall.originalWith
 * @see SuperCall.superOf
 * @see SuperCall.superWith
 */
public infix fun <T> AnsweringScope<T>.calls(superCall: SuperCall) {
    answers(SuperCallAnswer(superCall))
}
