@file:Suppress("unused")

package dev.mokkery.internal

import dev.mokkery.MokkerySuiteScope
import dev.mokkery.internal.calls.CallTrace
import dev.mokkery.internal.calls.TemplatingScope
import dev.mokkery.internal.context.MocksRegistry
import dev.mokkery.internal.context.tools
import dev.mokkery.internal.names.createGroupMockReceiverShortener
import dev.mokkery.internal.utils.getValue
import dev.mokkery.internal.utils.instances
import dev.mokkery.internal.utils.orEmpty
import dev.mokkery.internal.utils.plus
import dev.mokkery.internal.utils.runSuspension
import dev.mokkery.matcher.ArgMatchersScope
import dev.mokkery.verify.VerifyMode

internal fun MokkerySuiteScope.internalVerifySuspend(
    scope: TemplatingScope,
    mode: VerifyMode,
    block: suspend ArgMatchersScope.() -> Unit
) = internalVerify(scope, mode) { runSuspension { block() } }

internal fun MokkerySuiteScope.internalVerify(
    templating: TemplatingScope,
    mode: VerifyMode,
    block: ArgMatchersScope.() -> Unit
) {
    val verifier = tools.verifierFactory.create(mode)
    val result = runCatching { block(templating) }
    val exception = result.exceptionOrNull()
    if (exception != null && exception !is DefaultNothingException) {
        templating.release()
        throw exception
    }
    val mocks = mokkeryContext[MocksRegistry]?.mocks.orEmpty() + templating.mocks
    val calls = mocks
        .instances
        .map { it.mokkeryInterceptor.callTracing.unverified }
        .flatten()
        .sortedBy(CallTrace::orderStamp)
    val shortener = tools.createGroupMockReceiverShortener()
    shortener.prepare(calls, templating.templates)
    try {
        verifier
            .verify(shortener.shortenTraces(calls), shortener.shortenTemplates(templating.templates))
            .map(shortener::getOriginalTrace)
            .forEach { mocks.getValue(it.receiver).mokkeryInterceptor.callTracing.markVerified(it) }
    } finally {
        templating.release()
    }
}
