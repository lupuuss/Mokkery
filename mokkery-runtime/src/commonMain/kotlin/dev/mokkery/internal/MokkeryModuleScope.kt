@file:Suppress("unused")

package dev.mokkery.internal

import dev.mokkery.MockMode
import dev.mokkery.MokkeryScope
import dev.mokkery.internal.context.Settings
import dev.mokkery.internal.context.moduleNameContext
import dev.mokkery.verify.VerifyMode

@PublishedApi
internal fun MokkeryScope.createModuleScope(
    moduleName: String?,
    defaultMockMode: MockMode,
    defaultVerifyMode: VerifyMode,
): MokkeryScope = MokkeryScope(
    mokkeryContext
            + Settings(defaultMockMode, defaultVerifyMode)
            + moduleNameContext(moduleName)
)
