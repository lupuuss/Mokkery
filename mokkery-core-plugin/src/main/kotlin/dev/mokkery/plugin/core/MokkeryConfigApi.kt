package dev.mokkery.plugin.core

import dev.mokkery.internal.options.MokkeryOption
import org.jetbrains.kotlin.config.CompilerConfiguration

fun <T> CompilerConfiguration.getSingleOrDefault(option: MokkeryOption<T>): T {
    return get(option.configurationKey)
        ?.singleOrNull()
        ?: option.defaultValue
        ?: error("No value for ${option.configurationKey}")
}
