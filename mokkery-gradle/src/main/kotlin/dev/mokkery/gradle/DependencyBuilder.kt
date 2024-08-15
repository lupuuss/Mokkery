package dev.mokkery.gradle

import dev.mokkery.MokkeryConfig
import org.gradle.api.artifacts.dsl.DependencyHandler
import org.jetbrains.kotlin.gradle.plugin.KotlinDependencyHandler

public fun DependencyHandler.mokkery(module: String, version: String? = null): String {
    return mokkeryDependency(module, version)
}

public fun KotlinDependencyHandler.mokkery(module: String, version: String? = null): String {
    return mokkeryDependency(module, version)
}

private fun mokkeryDependency(module: String, version: String?): String {
    return "${MokkeryConfig.GROUP}:mokkery-$module:${version ?: MokkeryConfig.VERSION}"
}
