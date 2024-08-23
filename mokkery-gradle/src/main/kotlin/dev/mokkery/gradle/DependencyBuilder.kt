package dev.mokkery.gradle

import dev.mokkery.MokkeryConfig
import org.gradle.api.artifacts.dsl.DependencyHandler
import org.jetbrains.kotlin.gradle.plugin.KotlinDependencyHandler

/**
 * Builds the dependency notation for the named Mokkery [module] at the given [version].
 * If [version] is not specified, it uses current Mokkery version ([MokkeryConfig.VERSION])
*/
public fun DependencyHandler.mokkery(module: String, version: String? = null): String {
    return mokkeryDependency(module, version)
}

/**
 * Builds the dependency notation for the named Mokkery [module] at the given [version].
 * If [version] is not specified, it uses current Mokkery version ([MokkeryConfig.VERSION])
 */
public fun KotlinDependencyHandler.mokkery(module: String, version: String? = null): String {
    return mokkeryDependency(module, version)
}

private fun mokkeryDependency(module: String, version: String?): String {
    return "${MokkeryConfig.GROUP}:mokkery-$module:${version ?: MokkeryConfig.VERSION}"
}
