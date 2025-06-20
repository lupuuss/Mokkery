@file:Suppress("NOTHING_TO_INLINE")

package dev.mokkery.internal.context

import dev.mokkery.MokkeryScope
import dev.mokkery.answering.autofill.AutofillProvider
import dev.mokkery.context.MokkeryContext
import dev.mokkery.context.require
import dev.mokkery.internal.Counter
import dev.mokkery.internal.MonotonicCounter
import dev.mokkery.internal.calls.CallMatcher
import dev.mokkery.internal.calls.CallMatcherFactory
import dev.mokkery.internal.names.CallTraceReceiverShortener
import dev.mokkery.internal.names.NameShortener
import dev.mokkery.internal.names.ReverseDomainNameShortener
import dev.mokkery.internal.names.SignatureGenerator
import dev.mokkery.internal.names.withTypeArgumentsSupport
import dev.mokkery.internal.utils.mokkeryRuntimeError
import dev.mokkery.internal.verify.VerifierFactory

internal val MokkeryScope.tools: MokkeryTools
    get() = mokkeryContext.require(MokkeryTools)

internal class MokkeryTools(
    namesShortener: NameShortener? = null,
    signatureGenerator: SignatureGenerator? = null,
    callTraceReceiverShortener: CallTraceReceiverShortener? = null,
    callMatcherFactory: CallMatcherFactory? = null,
    callsCounter: Counter? = null,
    mocksCounter: Counter? = null,
    autofillProvider: AutofillProvider<Any?>? = null,
    verifierFactory: VerifierFactory? = null,
) : MokkeryContext.Element {

    private val _namesShortener: NameShortener? = namesShortener
    private val _signatureGenerator: SignatureGenerator? = signatureGenerator
    private val _callTraceReceiverShortener: CallTraceReceiverShortener? = callTraceReceiverShortener
    private val _callMatcherFactory = callMatcherFactory
    private val _callsCounter = callsCounter
    private val _mocksCounter = mocksCounter
    private val _autofillProvider = autofillProvider
    private val _verifierFactory = verifierFactory

    val namesShortener: NameShortener
        get() = _namesShortener ?: mokkeryRuntimeError("NamesShortener not present in the tools!")
    val signatureGenerator: SignatureGenerator
        get() = _signatureGenerator ?: mokkeryRuntimeError("SignatureGenerator not present in the tools!")
    val callTraceReceiverShortener: CallTraceReceiverShortener
        get() = _callTraceReceiverShortener ?: mokkeryRuntimeError("CallTraceReceiverShortener not present in the tools!")
    val callMatcherFactory: CallMatcherFactory
        get() = _callMatcherFactory ?: mokkeryRuntimeError("CallMatcher not present in call tools!")
    val callsCounter: Counter
        get() = _callsCounter ?: mokkeryRuntimeError("Calls Counter not present in tools!")
    val mocksCounter: Counter
        get() = _mocksCounter ?: mokkeryRuntimeError("Mocks Counter not present in tools!")
    val autofillProvider: AutofillProvider<Any?>
        get() = _autofillProvider ?: mokkeryRuntimeError("AutofillProvider not present in tools!")
    val verifierFactory: VerifierFactory
        get() = _verifierFactory ?: mokkeryRuntimeError("VerifierFactory not present in tools!")

    override val key = Key

    fun copy(
        namesShortener: NameShortener? = _namesShortener,
        signatureGenerator: SignatureGenerator? = _signatureGenerator,
        callTraceReceiverShortener: CallTraceReceiverShortener? = _callTraceReceiverShortener,
        callMatcherFactory: CallMatcherFactory? = _callMatcherFactory,
        callsCounter: Counter? = _callsCounter,
        mocksCounter: Counter? = _mocksCounter,
        autofillProvider: AutofillProvider<Any?>? = _autofillProvider,
        verifierFactory: VerifierFactory? = _verifierFactory
    ) = MokkeryTools(
        namesShortener = namesShortener,
        signatureGenerator = signatureGenerator,
        callTraceReceiverShortener = callTraceReceiverShortener,
        callMatcherFactory = callMatcherFactory,
        callsCounter = callsCounter,
        mocksCounter = mocksCounter,
        autofillProvider = autofillProvider,
        verifierFactory = verifierFactory
    )

    override fun toString(): String = "MokkeryTools@${hashCode()}"

    companion object Key : MokkeryContext.Key<MokkeryTools> {

        fun default(): MokkeryTools {
            val mocksCounter = MonotonicCounter(1)
            val namesShortener = ReverseDomainNameShortener.withTypeArgumentsSupport()
            val signatureGenerator = SignatureGenerator()
            val callMatcherFactory = CallMatcherFactory(signatureGenerator)
            return MokkeryTools(
                namesShortener = namesShortener,
                signatureGenerator = signatureGenerator,
                callTraceReceiverShortener = CallTraceReceiverShortener(namesShortener),
                callMatcherFactory = callMatcherFactory,
                callsCounter = MonotonicCounter(Long.MIN_VALUE),
                mocksCounter = mocksCounter,
                autofillProvider = AutofillProvider.forInternals,
                verifierFactory = VerifierFactory(callMatcherFactory)
            )
        }
    }
}
