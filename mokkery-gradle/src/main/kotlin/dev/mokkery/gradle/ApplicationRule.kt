package dev.mokkery.gradle

import org.jetbrains.kotlin.gradle.plugin.KotlinSourceSet

fun interface ApplicationRule {

    fun isApplicable(sourceSet: KotlinSourceSet): Boolean

    data class MatchesName(val regex: Regex) : ApplicationRule {
        override fun isApplicable(sourceSet: KotlinSourceSet) = sourceSet.name.matches(regex)
    }

    data class Listed(private val elements: List<String>) : ApplicationRule {

        constructor(vararg elements: String) : this(elements.toList())

        override fun isApplicable(sourceSet: KotlinSourceSet): Boolean {
            return sourceSet.name in elements
        }
    }

    companion object {

        val AllTests: ApplicationRule = MatchesName(Regex(".*Test.*"))
    }
}
