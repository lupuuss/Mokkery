package dev.mokkery.plugin.ir.transformers.core

import dev.mokkery.MockMode
import dev.mokkery.internal.options.MokkeryOption
import dev.mokkery.internal.options.MokkeryOptions
import dev.mokkery.options.AnnotationSelector
import dev.mokkery.plugin.MembersValidationMode
import dev.mokkery.plugin.configurationKey
import dev.mokkery.plugin.ir.stubs.MokkeryStubsConfig
import dev.mokkery.verify.VerifyMode
import org.jetbrains.kotlin.config.CompilerConfiguration

val TransformerScope.mockMode: MockMode
    get() = compilerConfig.getSingleOrDefault(MokkeryOptions.Core.defaultMockMode)

val TransformerScope.verifyMode: VerifyMode
    get() = compilerConfig.getSingleOrDefault(MokkeryOptions.Core.defaultVerifyMode)

val CompilerConfiguration.validationMode: MembersValidationMode get() = when {
    getSingleOrDefault(MokkeryOptions.Core.ignoreFinalMembers) -> MembersValidationMode.IgnoreFinal
    getSingleOrDefault(MokkeryOptions.Core.ignoreInlineMembers) -> MembersValidationMode.IgnoreInline
    else -> MembersValidationMode.Strict
}

val CompilerConfiguration.enableFirDiagnostics: Boolean
    get() = getSingleOrDefault(MokkeryOptions.Core.enableFirDiagnostics)

val CompilerConfiguration.stubsConfig: MokkeryStubsConfig
    get() = MokkeryStubsConfig(
        allowClassInheritance = getSingleOrDefault(MokkeryOptions.Stubs.allowClassInheritance),
        allowConcreteClassInstantiation = getSingleOrDefault(MokkeryOptions.Stubs.allowConcreteClassInstantiation)
    )

val CompilerConfiguration.annotationSelector: AnnotationSelector
    get() = getSingleOrDefault(MokkeryOptions.Annotations.copyToMock)

fun <T> CompilerConfiguration.getSingleOrDefault(option: MokkeryOption<T>): T {
    return get(option.configurationKey)
        ?.singleOrNull()
        ?: option.defaultValue
        ?: error("No value for ${option.configurationKey}")
}
