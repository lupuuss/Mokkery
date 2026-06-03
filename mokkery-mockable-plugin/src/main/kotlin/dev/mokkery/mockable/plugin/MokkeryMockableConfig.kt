package dev.mokkery.mockable.plugin

import dev.mokkery.mockable.internal.options.MokkeryMockableOptions
import dev.mokkery.plugin.core.getAllOrDefault
import dev.mokkery.plugin.core.getSingleOrDefault
import org.jetbrains.kotlin.config.CompilerConfiguration
import org.jetbrains.kotlin.name.FqName

val CompilerConfiguration.annotationFqNames: Set<FqName>
    get() = getAllOrDefault(MokkeryMockableOptions.annotations)
        .mapTo(mutableSetOf(), ::FqName)

val CompilerConfiguration.enableFirDiagnostics: Boolean
    get() = getSingleOrDefault(MokkeryMockableOptions.enableFirDiagnostics)
