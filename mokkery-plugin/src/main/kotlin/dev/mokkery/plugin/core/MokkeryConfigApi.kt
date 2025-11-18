package dev.mokkery.plugin.core

import dev.mokkery.MockMode
import dev.mokkery.MokkeryCompilerDefaults
import dev.mokkery.plugin.ENABLE_FIR_DIAGNOSTICS
import dev.mokkery.plugin.IGNORE_FINAL_MEMBERS
import dev.mokkery.plugin.IGNORE_INLINE_MEMBERS
import dev.mokkery.plugin.MOCK_MODE_KEY
import dev.mokkery.plugin.VERIFY_MODE_KEY
import dev.mokkery.verify.VerifyMode
import org.jetbrains.kotlin.config.CompilerConfiguration

val TransformerScope.mockMode: MockMode get() = compilerConfig
    .get(MOCK_MODE_KEY)
    ?.singleOrNull()
    ?: MokkeryCompilerDefaults.mockMode

val TransformerScope.verifyMode: VerifyMode get() = compilerConfig
    .get(VERIFY_MODE_KEY)
    ?.singleOrNull()
    ?: MokkeryCompilerDefaults.verifyMode


val CompilerConfiguration.validationMode: MembersValidationMode get() =  when {
    get(IGNORE_FINAL_MEMBERS)?.singleOrNull() == true -> MembersValidationMode.IgnoreFinal
    get(IGNORE_INLINE_MEMBERS)?.singleOrNull() == true -> MembersValidationMode.IgnoreInline
    else -> MembersValidationMode.Strict
}

val CompilerConfiguration.enableFirDiagnostics: Boolean
    get() = get(ENABLE_FIR_DIAGNOSTICS)?.singleOrNull() ?: true
