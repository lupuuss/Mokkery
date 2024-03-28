package dev.mokkery.internal.verify

import dev.mokkery.internal.MockUniqueReceiversGenerator
import dev.mokkery.internal.names.NameShortener
import dev.mokkery.internal.names.ReverseDomainNameShortener
import dev.mokkery.internal.templating.CallTemplate
import dev.mokkery.internal.tracing.CallTrace

internal class MockReceiverShortener(
    private val namesShortener: NameShortener = ReverseDomainNameShortener,
    private val receiversGenerator: MockUniqueReceiversGenerator = MockUniqueReceiversGenerator
) {

    private lateinit var names: Map<String, String>
    private lateinit var callsMapping: Map<CallTrace, CallTrace>

    fun prepare(calls: List<CallTrace>, templates: List<CallTemplate>) {
        val names = mutableSetOf<String>()
        calls.mapTo(names) { receiversGenerator.extractType(it.receiver) }
        templates.mapTo(names) { receiversGenerator.extractType(it.receiver) }
        this.names = namesShortener.shorten(names)
    }

    fun shortenTemplates(templates: List<CallTemplate>): List<CallTemplate> = templates.map {
        val noIdReceiver = receiversGenerator.extractType(it.receiver)
        val id = it.receiver.removePrefix(noIdReceiver)
        it.copy(receiver = "${names.getValue(noIdReceiver)}$id")
    }

    fun shortenTraces(calls: List<CallTrace>): List<CallTrace> {
        val callsMapping = linkedMapOf<CallTrace, CallTrace>()
        calls.associateByTo(callsMapping) {
            val noIdReceiver = receiversGenerator.extractType(it.receiver)
            val id = it.receiver.removePrefix(noIdReceiver)
            it.copy(receiver = "${names.getValue(noIdReceiver)}$id")
        }
        this.callsMapping = callsMapping
        return callsMapping.keys.toList()
    }

    fun getOriginalTrace(trace: CallTrace): CallTrace = callsMapping.getValue(trace)
}
