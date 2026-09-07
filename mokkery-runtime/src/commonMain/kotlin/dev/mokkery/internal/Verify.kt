@file:Suppress("unused")

package dev.mokkery.internal

import dev.mokkery.MokkeryScope
import dev.mokkery.internal.annotations.Templating
import dev.mokkery.internal.context.MokkeryInstancesRegistry
import dev.mokkery.internal.context.settings
import dev.mokkery.internal.context.tools
import dev.mokkery.internal.templating.templatingScope
import dev.mokkery.internal.templating.participatingInstances
import dev.mokkery.internal.templating.registeredTemplates
import dev.mokkery.internal.tracing.withVerifySession
import dev.mokkery.internal.utils.runSuspension
import dev.mokkery.internal.verify.Verifier
import dev.mokkery.internal.verify.render.noMoreCalls
import dev.mokkery.internal.verify.render.verifierError
import dev.mokkery.internal.verify.render.verifyRendering
import dev.mokkery.templating.MokkeryTemplatingScope
import dev.mokkery.verify.VerifyMode

@PublishedApi
internal fun MokkeryScope.internalVerifySuspend(
    mode: VerifyMode?,
    block: @Templating suspend MokkeryTemplatingScope.() -> Unit
): Unit = internalVerify(mode) { runSuspension { block() } }

@PublishedApi
internal fun MokkeryScope.internalVerify(
    mode: VerifyMode?,
    block: @Templating MokkeryTemplatingScope.() -> Unit
) {
    val scope = templatingScope().apply(block)
    val instances = scope.participatingInstances
    val templates = scope.registeredTemplates
    val mode = mode ?: scope.settings.defaultVerifyMode
    instances.withVerifySession {
        val result = tools
            .verifierFactory
            .create(mode, instances)
            .verify(this.unverified, templates)
        when (result) {
            is Verifier.Result.Success -> result.verified.forEach { this.markVerified(it) }
            is Verifier.Result.Failure -> verifyRendering(instances) {
                throw AssertionError(verifierError.render(result.error))
            }
        }
    }
}

@PublishedApi
internal fun MokkeryScope.internalVerifyNoMoreCalls(vararg mocks: Any) {
    val collection = mokkeryContext[MokkeryInstancesRegistry]
        ?.collection
        .orEmpty() + mocks.map(Any::requireInstanceScope).toMokkeryCollection()
    collection.withVerifySession {
        val unverifiedCalls = unverified
        if (unverifiedCalls.isEmpty()) return@withVerifySession
        verifyRendering(collection) {
            throw AssertionError(noMoreCalls.render(unverifiedCalls))
        }
    }
}
