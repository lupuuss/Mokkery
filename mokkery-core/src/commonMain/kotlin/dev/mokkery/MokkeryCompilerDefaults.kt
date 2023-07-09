package dev.mokkery

import dev.mokkery.annotations.InternalMokkeryApi
import dev.mokkery.verify.VerifyMode

/**
 * Contains defaults for Mokkery. Plugin only uses those values if they are not specified by the user.
 * Those values do not represent actual runtime defaults.
 */
@InternalMokkeryApi
public object MokkeryCompilerDefaults {
    public val mockMode: MockMode = MockMode.strict
    public val verifyMode: VerifyMode = VerifyMode.soft
}
