package dev.mokkery

import dev.mokkery.annotations.InternalMokkeryApi
import dev.mokkery.verify.VerifyMode

/**
 * Contains defaults for mokkery. Plugin only sets those values if there are not specified by the user.
 * Those values do not represent actual runtime defaults.
 */
@InternalMokkeryApi
public object MokkeryCompilerDefaults {
    public val mockMode: MockMode = MockMode.strict
    public val verifyMode: VerifyMode = VerifyMode.soft
}
