package dev.mokkery.plugin.core

import dev.mokkery.MockMode
import dev.mokkery.plugin.IGNORE_FINAL_MEMBERS
import dev.mokkery.plugin.IGNORE_INLINE_MEMBERS
import dev.mokkery.plugin.MOCK_MODE_KEY
import dev.mokkery.plugin.VERIFY_MODE_KEY
import dev.mokkery.verify.VerifyMode
import org.jetbrains.kotlin.config.CompilerConfiguration

val TransformerScope.mockMode: MockMode get() = compilerConfig.get(MOCK_MODE_KEY)!!.single()
val TransformerScope.verifyMode: VerifyMode get() = compilerConfig.get(VERIFY_MODE_KEY)!!.single()
val CompilerConfiguration.validationMode: MembersValidationMode get() =  when {
    get(IGNORE_FINAL_MEMBERS)?.singleOrNull() == true -> MembersValidationMode.IgnoreFinal
    get(IGNORE_INLINE_MEMBERS)?.singleOrNull() == true -> MembersValidationMode.IgnoreInline
    else -> MembersValidationMode.Strict
}
