package dev.mokkery.gradle

import dev.mokkery.MockMode
import dev.mokkery.verify.VerifyMode
import org.gradle.api.provider.Property

public interface MokkeryGradleExtension {
    /**
     * Determines source sets to be affected by Mokkery.
     *
     * By default, it is [ApplicationRule.AllTests]
     */
    public val rule: Property<ApplicationRule>

    /**
     * Determines default mock mode for created mocks.
     *
     * By default, it is [MockMode.strict].
     */
    public val defaultMockMode: Property<MockMode>

    /**
     * Determines default verification mode for `verify` calls.
     *
     * By default, it is [VerifyMode.soft].
     */
    public val defaultVerifyMode: Property<VerifyMode>

    /**
     * Allows creating mocks of open/abstract classes with `inline` members.
     * Compiler plugin no longer fails with inline members. However, it is not possible to change or track
     * original behaviour of inline members.
     *
     * By default, it is disabled.
     */
    public val ignoreInlineMembers: Property<Boolean>

    /**
     * Allows creating mocks of open/abstract classes with `final` members.
     * Compiler plugin no longer fails with final members. However, it is not possible to change or track
     * original behaviour of final members.
     *
     * By default, it is disabled.
     */
    public val ignoreFinalMembers: Property<Boolean>
}
