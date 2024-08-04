package dev.mokkery.gradle

import dev.mokkery.MokkeryConfig
import org.gradle.api.artifacts.dsl.DependencyHandler

public fun DependencyHandler.mokkery(module: String, version: String? = null): String {
    return "${MokkeryConfig.GROUP}:mokkery-$module:${version ?: MokkeryConfig.VERSION}"
}