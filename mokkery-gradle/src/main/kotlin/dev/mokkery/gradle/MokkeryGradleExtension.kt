package dev.mokkery.gradle

import dev.mokkery.MockMode
import dev.mokkery.MokkeryCompilerDefaults
import dev.mokkery.verify.VerifyMode

open class MokkeryGradleExtension {
    var rule: ApplicationRule = ApplicationRule.AllTests
    var defaultMockMode: MockMode = MokkeryCompilerDefaults.mockMode
    var defaultVerifyMode: VerifyMode = MokkeryCompilerDefaults.verifyMode
}
