package dev.mokkery.plugin

import dev.mokkery.internal.options.MokkeryOption
import dev.mokkery.internal.options.MokkeryOptionProjection
import dev.mokkery.internal.options.get
import org.jetbrains.kotlin.compiler.plugin.CliOption
import org.jetbrains.kotlin.config.CompilerConfigurationKey

val MokkeryOption<*>.cliOption: CliOption
    get() = get(CliOptionProjection)

@Suppress("UNCHECKED_CAST")
val <T> MokkeryOption<T>.configurationKey: CompilerConfigurationKey<List<T>>
    get() = get(ConfigurationKeyProjection) as CompilerConfigurationKey<List<T>>

private val CliOptionProjection = MokkeryOptionProjection.cached {
    CliOption(
        optionName = it.name,
        valueDescription = it.type.description,
        description = it.description,
        required = it.required,
        allowMultipleOccurrences = it.allowMultipleOccurrences,
    )
}

private val ConfigurationKeyProjection = MokkeryOptionProjection.cached<CompilerConfigurationKey<List<Any?>>> {
    CompilerConfigurationKey.create(it.name)
}
