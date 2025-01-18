package dev.mokkery.internal.context

import dev.mokkery.context.MokkeryContext
import dev.mokkery.context.require
import dev.mokkery.internal.Counter
import dev.mokkery.internal.MokkeryInstance
import dev.mokkery.internal.MokkeryInstanceLookup
import dev.mokkery.internal.MokkeryMockInstance
import dev.mokkery.internal.MonotonicCounter
import dev.mokkery.internal.calls.ArgMatchersComposer
import dev.mokkery.internal.calls.CallMatcher
import dev.mokkery.internal.names.CallTraceReceiverShortener
import dev.mokkery.internal.names.MokkeryInstanceIdGenerator
import dev.mokkery.internal.names.NameShortener
import dev.mokkery.internal.names.ReverseDomainNameShortener
import dev.mokkery.internal.names.SignatureGenerator
import dev.mokkery.internal.names.UniqueMokkeryInstanceIdGenerator
import dev.mokkery.internal.names.withTypeArgumentsSupport

internal val MokkeryContext.tools: MokkeryTools
    get() = require(MokkeryTools)

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

    val instanceLookup: MokkeryInstanceLookup
        get() = _instanceLookup ?: error("MokkeryInstanceLookup not present in the tools!")
    val namesShortener: NameShortener
        get() = _namesShortener ?: error("NamesShortener not present in the tools!")
    val instanceIdGenerator: MokkeryInstanceIdGenerator
        get() = _instanceIdGenerator ?: error("MokkeryInstanceIdGenerator not present in the tools!")
    val signatureGenerator: SignatureGenerator
        get() = _signatureGenerator ?: error("SignatureGenerator not present in the tools!")
    val callTraceReceiverShortener: CallTraceReceiverShortener
        get() = _callTraceReceiverShortener ?: error("CallTraceReceiverShortener not present in the tools!")
    val callMatcher: CallMatcher
        get() = _callMatcher ?: error("CallMatcher not present in call tools!")
    val argMatchersComposer: ArgMatchersComposer
        get() = _argMatchersComposer ?: error("ArgMatchersComposer not present in tools!")
    val callsCounter: Counter
        get() = _callsCounter ?: error("Calls Counter not present in tools!")
    val mocksCounter: Counter
        get() = _mocksCounter ?: error("Mocks Counter not present in tools!")

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
    )

    override val key = Key

    companion object Key : MokkeryContext.Key<MokkeryTools> {

        fun default(): MokkeryTools {
            val mocksCounter = MonotonicCounter(1)
            val instanceIdGenerator = UniqueMokkeryInstanceIdGenerator(mocksCounter)
            val namesShortener = ReverseDomainNameShortener.withTypeArgumentsSupport()
            val signatureGenerator = SignatureGenerator()
            return MokkeryTools(
                instanceLookup = MokkeryInstanceLookup(),
                namesShortener = namesShortener,
                instanceIdGenerator = instanceIdGenerator,
                signatureGenerator = signatureGenerator,
                callTraceReceiverShortener = CallTraceReceiverShortener(instanceIdGenerator, namesShortener),
                callMatcher = CallMatcher(signatureGenerator),
                argMatchersComposer = ArgMatchersComposer(),
                callsCounter = MonotonicCounter(Long.MIN_VALUE),
                mocksCounter = mocksCounter
            )
        }
    }
}

internal fun MokkeryTools.resolveMockInstance(obj: Any?): MokkeryMockInstance? {
    return instanceLookup.resolve(obj) as? MokkeryMockInstance
}

internal fun MokkeryTools.reverseResolveInstance(instance: MokkeryInstance): Any? {
    return instanceLookup.reverseResolve(instance)
}
