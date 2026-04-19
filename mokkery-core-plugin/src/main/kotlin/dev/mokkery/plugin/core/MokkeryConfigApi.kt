package dev.mokkery.plugin.core

import dev.mokkery.internal.options.MokkeryOption
import org.jetbrains.kotlin.config.CompilerConfiguration

fun <T> CompilerConfiguration.getSingleOrDefault(option: MokkeryOption<T>): T {
    require(!option.allowMultipleOccurrences) { "`getSingleOrDefault` is not supported for `allowMultipleOccurrences = true`" }
    return get(option.configurationKey)
        ?.singleOrNull()
        ?: option.defaultValues.singleOrNull()
        ?: error("No value for ${option.configurationKey}")
}

fun <T> CompilerConfiguration.getAllOrDefault(option: MokkeryOption<T>): List<T> = get(option.configurationKey)
    ?: option.defaultValues
