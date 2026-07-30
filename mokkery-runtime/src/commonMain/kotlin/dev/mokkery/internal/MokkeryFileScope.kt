package dev.mokkery.internal

import dev.mokkery.MockMode
import dev.mokkery.MokkeryScope
import dev.mokkery.context.MokkeryContext
import dev.mokkery.internal.context.Settings
import dev.mokkery.internal.context.settings
import dev.mokkery.verify.VerifyMode

@Suppress("unused")
@PublishedApi
internal fun MokkeryScope.fileContext(
    defaultMockMode: MockMode,
    defaultVerifyMode: VerifyMode,
): MokkeryContext {
    val current = settings
    return when {
        current.defaultVerifyMode == defaultVerifyMode && current.defaultMockMode == defaultMockMode -> mokkeryContext
        else -> mokkeryContext + Settings(defaultMockMode, defaultVerifyMode)
    }
}
