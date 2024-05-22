package dev.mokkery.matcher.varargs

import dev.drewhamilton.poko.Poko
import dev.mokkery.annotations.DelicateMokkeryApi
import dev.mokkery.internal.asListOrNull
import dev.mokkery.internal.capitalize
import dev.mokkery.internal.unsafeCast
import dev.mokkery.internal.varargNameByElementType
import dev.mokkery.matcher.ArgMatcher
import dev.mokkery.matcher.varargs.VarArgMatcher.Base
import kotlin.reflect.KClass

/**
 * Wildcard vararg matcher that checks a subset of varargs. It can occur only once.
 * To provide your own implementation use [Base].
 */
public sealed interface VarArgMatcher : ArgMatcher<Any?> {


    /**
     * Base class for any [VarArgMatcher]. Returns false if arg is not an array or list.
     */
    public abstract class Base<in T> : VarArgMatcher {

        final override fun matches(arg: Any?): Boolean {
            val varargs = arg.asListOrNull() ?: return false
            return matchesVarargs(varargs.unsafeCast())
        }

        public abstract fun matchesVarargs(varargs: List<T>): Boolean
    }

    /**
     * Matches a sequence of varargs with all elements matching the [predicate].
     * @param type of vararg element to provide vararg type information in [AllThat.toString]
     */
    @DelicateMokkeryApi
    @Poko
    public class AllThat<T>(private val type: KClass<*>, private val predicate: (T) -> Boolean) : Base<T>() {

        override fun matchesVarargs(varargs: List<T>): Boolean = varargs.all(predicate)

        override fun toString(): String = "${varargNameByElementType(type)}All {...}"
    }

    /**
     * Matches a sequence of varargs with any element matching the [predicate].
     * @param type of vararg element to provide vararg type information in [AnyThat.toString]
     */
    @DelicateMokkeryApi
    @Poko
    public class AnyThat<T>(private val type: KClass<*>, private val predicate: (T) -> Boolean) : Base<T>() {

        override fun matchesVarargs(varargs: List<T>): Boolean = varargs.any(predicate)

        override fun toString(): String = "${varargNameByElementType(type)}Any { ... }"
    }

    /**
     * Matches any sequence of varargs.
     * @param type of vararg element to provide vararg type information in [AnyOf.toString]
     */
    @DelicateMokkeryApi
    @Poko
    public class AnyOf(private val type: KClass<*>) : Base<Any?>() {

        override fun matchesVarargs(varargs: List<Any?>): Boolean = true

        override fun toString(): String = "any${varargNameByElementType(type).capitalize()}()"
    }
}

