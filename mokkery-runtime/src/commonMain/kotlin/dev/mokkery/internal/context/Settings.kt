package dev.mokkery.internal.context

import dev.mokkery.MockMode
import dev.mokkery.MokkeryCompilerDefaults
import dev.mokkery.MokkeryScope
import dev.mokkery.context.MokkeryContext
import dev.mokkery.context.require
import dev.mokkery.verify.VerifyMode

internal val MokkeryScope.settings: Settings
    get() = mokkeryContext.require(Settings)

internal data class Settings(
    val defaultMockMode: MockMode,
    val defaultVerifyMode: VerifyMode,
): MokkeryContext.Element {

    override val key get() = Key

    companion object Key : MokkeryContext.Key<Settings> {

        fun default() = Settings(
            MokkeryCompilerDefaults.mockMode,
            MokkeryCompilerDefaults.verifyMode,
        )
    }
}
