package dev.mokkery.gradle

import org.jetbrains.kotlin.gradle.plugin.KotlinSourceSet

/**
 * Determines if Mokkery should be applied to given [KotlinSourceSet] or not.
 */
public fun interface ApplicationRule {

    public fun isApplicable(sourceSet: KotlinSourceSet): Boolean

    /**
     * Results in Mokkery being applied to all source sets matching [regex].
     */
    public data class MatchesName(val regex: Regex) : ApplicationRule {
        public override fun isApplicable(sourceSet: KotlinSourceSet): Boolean = sourceSet.name.matches(regex)
    }

    /**
     * Results in Mokkery being applied to all source sets with specified names.
     */
    public data class Listed(private val elements: List<String>) : ApplicationRule {

        public constructor(vararg elements: String) : this(elements.toList())

        override fun isApplicable(sourceSet: KotlinSourceSet): Boolean {
            return sourceSet.name in elements
        }
    }

    public companion object {

        /**
         * Results in Mokkery being applied to all source sets with `Test` phrase.
         */
        public val AllTests: ApplicationRule = MatchesName(Regex(".*Test.*"))
    }
}
