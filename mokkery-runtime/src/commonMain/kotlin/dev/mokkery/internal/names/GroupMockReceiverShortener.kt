package dev.mokkery.internal.names

import dev.mokkery.internal.calls.CallTemplate
import dev.mokkery.internal.calls.CallTrace
import dev.mokkery.internal.context.MokkeryTools
import kotlin.collections.associateByTo
import kotlin.collections.getValue
import kotlin.collections.map
import kotlin.collections.mapTo
import kotlin.collections.toList

internal fun MokkeryTools.createGroupMockReceiverShortener() = GroupMockReceiverShortener(namesShortener)

internal class GroupMockReceiverShortener(
    private val namesShortener: NameShortener,
) {

    private lateinit var names: Map<String, String>
    private lateinit var callsMapping: Map<CallTrace, CallTrace>

    fun prepare(calls: List<CallTrace>, templates: List<CallTemplate>) {
        val names = mutableSetOf<String>()
        calls.mapTo(names) { it.mockId.typeName }
        templates.mapTo(names) { it.mockId.typeName }
        this.names = namesShortener.shorten(names)
    }

    fun shortenTemplates(templates: List<CallTemplate>): List<CallTemplate> = templates.map {
        val id = it.mockId
        it.copy(mockId = id.copy(typeName = names.getValue(id.typeName)))
    }

    fun shortenTraces(calls: List<CallTrace>): List<CallTrace> {
        val callsMapping = linkedMapOf<CallTrace, CallTrace>()
        calls.associateByTo(callsMapping) {
            val id = it.mockId
            it.copy(mockId = id.copy(typeName = names.getValue(id.typeName)))
        }
        this.callsMapping = callsMapping
        return callsMapping.keys.toList()
    }

    fun getOriginalTrace(trace: CallTrace): CallTrace = callsMapping.getValue(trace)
}
