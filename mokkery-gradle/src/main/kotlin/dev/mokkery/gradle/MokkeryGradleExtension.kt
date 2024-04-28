package dev.mokkery.gradle

import dev.mokkery.MockMode
import dev.mokkery.verify.VerifyMode
import org.gradle.api.provider.Property

interface MokkeryGradleExtension {
    val rule: Property<ApplicationRule>
    val defaultMockMode: Property<MockMode>
    val defaultVerifyMode: Property<VerifyMode>
    val allowIndirectSuperCalls: Property<Boolean>
    val ignoreInlineMembers: Property<Boolean>
    val ignoreFinalMembers: Property<Boolean>
}
