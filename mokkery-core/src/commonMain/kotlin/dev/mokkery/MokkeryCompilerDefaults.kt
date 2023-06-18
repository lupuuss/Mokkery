package dev.mokkery

import dev.mokkery.annotations.InternalMokkeryApi
import dev.mokkery.verify.VerifyMode

@InternalMokkeryApi
public object MokkeryCompilerDefaults {
    public val mockMode: MockMode = MockMode.strict
    public val verifyMode: VerifyMode = VerifyMode.soft
}
