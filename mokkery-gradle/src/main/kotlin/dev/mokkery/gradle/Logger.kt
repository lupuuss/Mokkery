package dev.mokkery.gradle

import dev.mokkery.internal.MokkeryConfig
import org.gradle.api.Project

internal fun Project.mokkeryInfo(message: String) = logger.info("${MokkeryConfig.PLUGIN_ID}: " + message)
