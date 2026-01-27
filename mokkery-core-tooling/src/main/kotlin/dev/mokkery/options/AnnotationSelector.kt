package dev.mokkery.options

import dev.drewhamilton.poko.Poko
import dev.mokkery.annotations.InternalMokkeryApi
import dev.mokkery.options.AnnotationSelector.Companion.none
import dev.mokkery.options.AnnotationSelectorInternals.All
import dev.mokkery.options.AnnotationSelectorInternals.Combined
import dev.mokkery.options.AnnotationSelectorInternals.Matches
import dev.mokkery.options.AnnotationSelectorInternals.Named
import dev.mokkery.options.AnnotationSelectorInternals.None

/**
 * Describes a composable selector used to match annotations.
 *
 * Selectors can be combined using `+`, negated using unary `-`,
 * or subtracted using `-` (which is equivalent to `+ (-other)`).
 *
 * Examples:
 * ```kotlin
 *
 * // all annotations except annotations named example.A and example.B
 * all - named("example.A", "example.B")
 *
 * // just annotations named example.A and example.B
 * named("example.A", "example.B")
 *
 * // all annotations except annotations with names matching "example.+" but including annotation example.A again
 * all - matches(Regex("example.+")) + named("example.A")
 * ```
 */
public sealed interface AnnotationSelector {

    /**
     * Combines this selector with another selector.
     *
     * Selectors are interpreted from left to right.
     * Negative selectors only makes sense when present on the left side of standard selector.
     * Example:
     * ```kotlin
     * // it's equivalent of just named("example.b")
     * -all + named("example.a")
     *
     * // it's equivalent of none, because `-all` removes everything from the right side.
     * named("example.a") - all
     * ```
     */
    public operator fun plus(other: AnnotationSelector): AnnotationSelector {
        if (other == none) return this
        if (other == all) return all
        if (other == -all) return none
        if (this !is Combined && other !is Combined) return Combined(listOf(this, other))
        var left = if (this is Combined) this.elements else listOf(this)
        var right = if (other is Combined) other.elements else listOf(other)
        while (true) {
            val lastLeft = left.lastOrNull() ?: break
            val firstRight = right.firstOrNull() ?: break
            val result = lastLeft + firstRight
            if (result is Combined) break
            left = left.dropLast(1) + result
            right = right.drop(1)
        }
        val combined = left + right
        if (combined.isEmpty()) return none
        if (combined.size == 1) return combined.first()
        return Combined(combined)
    }

    /**
     * Returns a selector that negates this selector.
     *
     * Negated selector will remove subset of annotations from regular selector.
     * Negated selector alone is equivalent of [none]. Read more about combinations in [plus] documentation.
     */
    public operator fun unaryMinus(): AnnotationSelector = AnnotationSelectorInternals.Minus(this)

    /**
     * Subtracts one selector from another.
     *
     * This is equivalent to `this + (-other)`.
     * Read more about combinations in [plus] documentation.
     */
    public operator fun minus(other: AnnotationSelector): AnnotationSelector = plus(-other)

    public companion object {

        /**
         * A selector that takes no annotations.
         */
        public val none: AnnotationSelector = None

        /**
         * A selector that takes all annotations.
         */
        public val all: AnnotationSelector = All

        /**
         * Creates a selector that takes annotations with the given [names].
         */
        public fun named(vararg names: String): AnnotationSelector = Named(names.toSet())

        /**
         * Creates a selector that takes annotations whose names match the given [regex].
         **/
        public fun matches(regex: Regex): AnnotationSelector = Matches(regex)

        /**
         * Creates a selector that takes annotations whose names match [Regex] with given [pattern] and [options].
         */
        public fun matches(
            pattern: String,
            vararg options: RegexOption
        ): AnnotationSelector = Matches(Regex(pattern, options.toSet()))
    }
}


@InternalMokkeryApi
public data object AnnotationSelectorInternals {

    @InternalMokkeryApi
    @Poko
    public class Combined internal constructor(public val elements: List<AnnotationSelector>) : AnnotationSelector {

        init {
            require(elements.size >= 2)
        }

        override fun toString(): String = buildString {
            append("(")
            append(elements[0].toString())
            for (i in 1..<elements.size) {
                append(' ')
                val element = elements[i]
                if (element is Minus) {
                    append('-')
                    append(' ')
                    append(element.selector.toString())
                } else {
                    append('+')
                    append(' ')
                    append(element.toString())
                }
            }
            append(")")
        }
    }

    @InternalMokkeryApi
    @Poko
    public class Minus(public val selector: AnnotationSelector) : AnnotationSelector {

        override fun plus(other: AnnotationSelector): AnnotationSelector {
            if (selector == other) return None
            if (other !is Minus) return super.plus(other)
            val result = selector + other.selector
            if (result is Combined) return super.plus(other)
            return Minus(result)
        }

        override fun unaryMinus(): AnnotationSelector = selector

        override fun toString(): String = "-$selector"
    }

    @InternalMokkeryApi
    @Poko
    public class Named internal constructor(public val names: Set<String>) : AnnotationSelector {

        override fun plus(other: AnnotationSelector): AnnotationSelector {
            if (other is Named) return Named(names + other.names)
            if (other !is Minus || other.selector !is Named) return super.plus(other)
            if (names == other.selector.names) return None
            if (names.containsAll(other.selector.names)) {
                return Named(names - other.selector.names)
            }
            if (other.selector.names.containsAll(names)) {
                return -Named(other.selector.names - names)
            }
            return super.plus(other)
        }

        override fun toString(): String = "named(${names.joinToString { "\"$it\"" }})"
    }

    @InternalMokkeryApi
    public class Matches internal constructor(public val regex: Regex) : AnnotationSelector {

        private val pattern get() = regex.pattern
        private val options get() = regex.options

        override fun toString(): String {
            if (regex.options.isEmpty()) return "matches(\"${regex.pattern}\")"
            return "matches(\"${regex.pattern}\", ${regex.options.joinToString(", ")})"
        }

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false
            other as Matches
            if (pattern != other.pattern) return false
            if (options != other.options) return false
            return true
        }

        override fun hashCode(): Int {
            var result = pattern.hashCode()
            result = 31 * result + options.hashCode()
            return result
        }
    }

    @InternalMokkeryApi
    public data object None : AnnotationSelector {

        override fun plus(other: AnnotationSelector): AnnotationSelector = other

        override fun unaryMinus(): AnnotationSelector = this

        override fun toString(): String = "none"

    }

    @InternalMokkeryApi
    public data object All : AnnotationSelector {

        private val minus: Minus = Minus(All)

        override fun plus(other: AnnotationSelector): AnnotationSelector {
            if (other is Minus) return super.plus(other)
            return this
        }

        override fun unaryMinus(): AnnotationSelector = minus

        override fun toString(): String = "all"
    }
}
