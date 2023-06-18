package dev.mokkery

import dev.mokkery.verify.VerifyMode

public object MokkeryCompilerDefaults {
    public val mockMode: MockMode = MockMode.Strict
    public val verifyMode: VerifyMode = VerifyMode.soft
}
