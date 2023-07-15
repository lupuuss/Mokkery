package dev.mokkery.matcher.varargs

import dev.mokkery.annotations.DelicateMokkeryApi
import dev.mokkery.internal.toListOrNull
import dev.mokkery.internal.capitalize
import dev.mokkery.internal.varargNameByElementType
import dev.mokkery.matcher.ArgMatcher
import kotlin.reflect.KClass

/**
 * Wildcard vararg matcher that checks a subset of varargs. It can occur only once.
 */
public interface VarArgMatcher : ArgMatcher<Any?> {


    /**
     * Matches an array with all elements matching the [predicate]. Returns false if it's not an array.
     * @param type of array element to provide vararg type information in [AnyOf.toString]
     */
    @DelicateMokkeryApi
    public class AllThat(
        private val type: KClass<*>,
        private val predicate: (Any?) -> Boolean
    ) : VarArgMatcher {
        override fun matches(arg: Any?): Boolean {
            val arrayAsList = arg.toListOrNull() ?: return false
            return arrayAsList.all(predicate)
        }

        override fun toString(): String = "${varargNameByElementType(type)} {...}"
    }

    /**
     * Matches any arg. It should be used only in context of varargs to not confuse the end user.
     * @param type of array element to provide vararg type information in [AnyOf.toString]
     */
    @DelicateMokkeryApi
    public class AnyOf(
        private val type: KClass<*>,
    ) : VarArgMatcher {
        override fun matches(arg: Any?): Boolean = true

        override fun toString(): String = "any${varargNameByElementType(type).capitalize()}()"

    }
}
