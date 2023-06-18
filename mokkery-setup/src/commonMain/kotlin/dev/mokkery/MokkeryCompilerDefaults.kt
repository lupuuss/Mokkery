package dev.mokkery

import dev.mokkery.verify.VerifyMode

public object MokkeryCompilerDefaults {
    public val mockMode: MockMode = MockMode.strict
    public val verifyMode: VerifyMode = VerifyMode.soft
}
