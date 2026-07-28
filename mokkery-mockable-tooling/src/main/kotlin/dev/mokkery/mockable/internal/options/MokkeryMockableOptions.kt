package dev.mokkery.mockable.internal.options

import dev.mokkery.annotations.InternalMokkeryApi
import dev.mokkery.internal.options.MokkeryOption
import dev.mokkery.internal.options.MokkeryOptionType.Companion.boolean
import dev.mokkery.internal.options.MokkeryOptionType.Companion.fqName
import dev.mokkery.internal.options.MokkeryOptionsContainer
import dev.mokkery.internal.options.MokkeryOptionsNamespace.Companion.root
import dev.mokkery.internal.options.defaultSingleOption
import dev.mokkery.internal.options.option

@InternalMokkeryApi
public object MokkeryMockableOptions : MokkeryOptionsContainer() {

    public val enableFirDiagnostics: MokkeryOption<Boolean> by root.defaultSingleOption(
        type = boolean,
        description = "Enables FIR diagnostics if raised.",
        defaultValue = true,
    )

    public val annotations: MokkeryOption<String> by root.option(
        type = fqName,
        description = "Annotation that marks classes for compiler transformation to make them mockable.",
        required = false,
        allowMultipleOccurrences = true,
        defaultValues = listOf("dev.mokkery.mockable.annotations.Mockable")
    )
}
