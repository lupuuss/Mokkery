@file:Suppress("unused")

package dev.mokkery.internal

import dev.mokkery.MokkeryScope
import dev.mokkery.internal.annotations.Templating
import dev.mokkery.internal.context.tools
import dev.mokkery.internal.templating.createTemplatingScope
import dev.mokkery.internal.templating.participatingInstances
import dev.mokkery.internal.templating.registeredTemplates
import dev.mokkery.internal.tracing.withTracingSession
import dev.mokkery.internal.utils.runSuspension
import dev.mokkery.templating.MokkeryTemplatingScope
import dev.mokkery.verify.VerifyMode

internal fun MokkeryScope.internalVerifySuspend(
    mode: VerifyMode,
    block: @Templating suspend MokkeryTemplatingScope.() -> Unit
) = internalVerify(mode) { runSuspension { block() } }

internal fun MokkeryScope.internalVerify(
    mode: VerifyMode,
    block: @Templating MokkeryTemplatingScope.() -> Unit
) {
    val scope = createTemplatingScope().apply(block)
    val instances = scope.participatingInstances
    instances.withTracingSession {
        tools
            .verifierFactory
            .create(mode, instances)
            .verify(this.unverified, scope.registeredTemplates)
            .forEach { this.markVerified(it) }
    }
}
