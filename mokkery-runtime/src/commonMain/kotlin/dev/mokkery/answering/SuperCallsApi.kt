package dev.mokkery.answering

import dev.drewhamilton.poko.Poko
import dev.mokkery.annotations.DelicateMokkeryApi
import dev.mokkery.internal.answering.SuperCallAnswer
import dev.mokkery.internal.context.GlobalMokkeryContext
import dev.mokkery.internal.context.tools
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
    @Poko
    public class OfType(public val type: KClass<*>, public val args: List<Any?>? = null) : SuperCall

    /**
     * Call for an original method of mocked type. If interface is mocked, default implementation is called.
     *
     * If [args] is null, args passed to mock ([FunctionScope.args]) are used.
     *
     * It is recommended to use [original] and [originalWith] instead.
     */
    @DelicateMokkeryApi
    @Poko
    public class Original(public val args: List<Any?>? = null) : SuperCall

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
    answers(SuperCallAnswer(superCall, GlobalMokkeryContext.tools.instanceLookup))
}
