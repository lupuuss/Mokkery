package dev.mokkery.plugin.core

import dev.mokkery.MockMode
import dev.mokkery.plugin.MOCK_MODE_KEY
import dev.mokkery.plugin.VERIFY_MODE_KEY
import dev.mokkery.verify.VerifyMode

val TransformerScope.mockMode: MockMode get() = compilerConfig.get(MOCK_MODE_KEY)!!.single()
val TransformerScope.verifyMode: VerifyMode get() = compilerConfig.get(VERIFY_MODE_KEY)!!.single()
