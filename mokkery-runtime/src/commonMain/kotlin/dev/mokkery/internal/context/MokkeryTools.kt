package dev.mokkery.internal.context

import dev.mokkery.MokkeryScope
import dev.mokkery.answering.autofill.AutofillProvider
import dev.mokkery.context.MokkeryContext
import dev.mokkery.context.require
import dev.mokkery.internal.Counter
import dev.mokkery.internal.MokkeryInstance
import dev.mokkery.internal.MokkeryInstanceLookup
import dev.mokkery.internal.MokkeryMockInstance
import dev.mokkery.internal.MonotonicCounter
import dev.mokkery.internal.ObjectNotMockedException
import dev.mokkery.internal.calls.ArgMatchersComposer
import dev.mokkery.internal.calls.CallMatcher
import dev.mokkery.internal.names.CallTraceReceiverShortener
import dev.mokkery.internal.names.MokkeryInstanceIdGenerator
import dev.mokkery.internal.names.NameShortener
import dev.mokkery.internal.names.ReverseDomainNameShortener
import dev.mokkery.internal.names.SignatureGenerator
import dev.mokkery.internal.names.UniqueMokkeryInstanceIdGenerator
import dev.mokkery.internal.names.withTypeArgumentsSupport
import dev.mokkery.internal.utils.mokkeryRuntimeError
import dev.mokkery.internal.verify.VerifierFactory

internal val MokkeryScope.tools: MokkeryTools
    get() = mokkeryContext.require(MokkeryTools)


internal class MokkeryTools(
    instanceLookup: MokkeryInstanceLookup? = null,
    namesShortener: NameShortener? = null,
    instanceIdGenerator: MokkeryInstanceIdGenerator? = null,
    signatureGenerator: SignatureGenerator? = null,
    callTraceReceiverShortener: CallTraceReceiverShortener? = null,
    callMatcher: CallMatcher? = null,
    argMatchersComposer: ArgMatchersComposer? = null,
    callsCounter: Counter? = null,
    mocksCounter: Counter? = null,
    autofillProvider: AutofillProvider<Any?>? = null,
    verifierFactory: VerifierFactory? = null,
) : MokkeryContext.Element {

    private val _instanceLookup: MokkeryInstanceLookup? = instanceLookup
    private val _namesShortener: NameShortener? = namesShortener
    private val _instanceIdGenerator: MokkeryInstanceIdGenerator? = instanceIdGenerator
    private val _signatureGenerator: SignatureGenerator? = signatureGenerator
    private val _callTraceReceiverShortener: CallTraceReceiverShortener? = callTraceReceiverShortener
    private val _callMatcher = callMatcher
    private val _argMatchersComposer = argMatchersComposer
    private val _callsCounter = callsCounter
    private val _mocksCounter = mocksCounter
    private val _autofillProvider = autofillProvider
    private val _verifierFactory = verifierFactory

    val instanceLookup: MokkeryInstanceLookup
        get() = _instanceLookup ?: mokkeryRuntimeError("MokkeryInstanceLookup not present in the tools!")
    val namesShortener: NameShortener
        get() = _namesShortener ?: mokkeryRuntimeError("NamesShortener not present in the tools!")
    val instanceIdGenerator: MokkeryInstanceIdGenerator
        get() = _instanceIdGenerator ?: mokkeryRuntimeError("MokkeryInstanceIdGenerator not present in the tools!")
    val signatureGenerator: SignatureGenerator
        get() = _signatureGenerator ?: mokkeryRuntimeError("SignatureGenerator not present in the tools!")
    val callTraceReceiverShortener: CallTraceReceiverShortener
        get() = _callTraceReceiverShortener ?: mokkeryRuntimeError("CallTraceReceiverShortener not present in the tools!")
    val callMatcher: CallMatcher
        get() = _callMatcher ?: mokkeryRuntimeError("CallMatcher not present in call tools!")
    val argMatchersComposer: ArgMatchersComposer
        get() = _argMatchersComposer ?: mokkeryRuntimeError("ArgMatchersComposer not present in tools!")
    val callsCounter: Counter
        get() = _callsCounter ?: mokkeryRuntimeError("Calls Counter not present in tools!")
    val mocksCounter: Counter
        get() = _mocksCounter ?: mokkeryRuntimeError("Mocks Counter not present in tools!")
    val autofillProvider: AutofillProvider<Any?>
        get() = _autofillProvider ?: mokkeryRuntimeError("AutofillProvider not present in tools!")
    val verifierFactory: VerifierFactory
        get() = _verifierFactory ?: mokkeryRuntimeError("VerifierFactory not present in tools!")

    fun copy(
        instanceLookup: MokkeryInstanceLookup? = _instanceLookup,
        namesShortener: NameShortener? = _namesShortener,
        instanceIdGenerator: MokkeryInstanceIdGenerator? = _instanceIdGenerator,
        signatureGenerator: SignatureGenerator? = _signatureGenerator,
        callTraceReceiverShortener: CallTraceReceiverShortener? = _callTraceReceiverShortener,
        callMatcher: CallMatcher? = _callMatcher,
        argMatchersComposer: ArgMatchersComposer? = _argMatchersComposer,
        callsCounter: Counter? = _callsCounter,
        mocksCounter: Counter? = _mocksCounter,
        autofillProvider: AutofillProvider<Any?>? = _autofillProvider,
        verifierFactory: VerifierFactory? = _verifierFactory
    ) = MokkeryTools(
        instanceLookup = instanceLookup,
        namesShortener = namesShortener,
        instanceIdGenerator = instanceIdGenerator,
        signatureGenerator = signatureGenerator,
        callTraceReceiverShortener = callTraceReceiverShortener,
        callMatcher = callMatcher,
        argMatchersComposer = argMatchersComposer,
        callsCounter = callsCounter,
        mocksCounter = mocksCounter,
        autofillProvider = autofillProvider,
        verifierFactory = verifierFactory
    )

    override val key = Key

    companion object Key : MokkeryContext.Key<MokkeryTools> {

        fun default(): MokkeryTools {
            val mocksCounter = MonotonicCounter(1)
            val instanceIdGenerator = UniqueMokkeryInstanceIdGenerator(mocksCounter)
            val namesShortener = ReverseDomainNameShortener.withTypeArgumentsSupport()
            val signatureGenerator = SignatureGenerator()
            val callMatcher = CallMatcher(signatureGenerator)
            return MokkeryTools(
                instanceLookup = MokkeryInstanceLookup(),
                namesShortener = namesShortener,
                instanceIdGenerator = instanceIdGenerator,
                signatureGenerator = signatureGenerator,
                callTraceReceiverShortener = CallTraceReceiverShortener(instanceIdGenerator, namesShortener),
                callMatcher = callMatcher,
                argMatchersComposer = ArgMatchersComposer(),
                callsCounter = MonotonicCounter(Long.MIN_VALUE),
                mocksCounter = mocksCounter,
                autofillProvider = AutofillProvider.forInternals,
                verifierFactory = VerifierFactory(callMatcher)
            )
        }
    }
}

internal fun MokkeryTools.resolveMockInstance(obj: Any?): MokkeryMockInstance {
    return instanceLookup.resolve(obj) as? MokkeryMockInstance ?: throw ObjectNotMockedException(obj)
}

internal fun MokkeryTools.reverseResolveInstance(instance: MokkeryInstance): Any {
    return instanceLookup.reverseResolve(instance) ?: mokkeryRuntimeError("Failed to reverse resolve $instance")
}


internal fun MokkeryTools.resolveMockInstanceOrNull(obj: Any?): MokkeryMockInstance? {
    return instanceLookup.resolve(obj) as? MokkeryMockInstance
}

internal fun MokkeryTools.reverseResolveInstanceOrNull(instance: MokkeryInstance): Any? {
    return instanceLookup.reverseResolve(instance)
}
