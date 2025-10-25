package dev.mokkery.internal.names

import dev.mokkery.internal.MokkeryInstanceId
import dev.mokkery.internal.MokkeryCollection
import dev.mokkery.internal.MokkeryInstanceScope
import dev.mokkery.internal.tracing.CallTrace
import dev.mokkery.internal.instanceId
import dev.mokkery.internal.templating.CallTemplate

internal interface AliasMokkeryCollection : MokkeryCollection {

    fun mapAliasToOriginal(alias: MokkeryInstanceId): MokkeryInstanceId

    fun mapOriginalToAlias(original: MokkeryInstanceId): MokkeryInstanceId
}

internal fun AliasMokkeryCollection.aliasTraces(
    traces: List<CallTrace>
): List<CallTrace> = traces.map { aliasTrace(it) }

internal fun AliasMokkeryCollection.aliasTemplates(
    templates: List<CallTemplate>
): List<CallTemplate> = templates.map { aliasTemplate(it) }

internal fun AliasMokkeryCollection.aliasTrace(
    trace: CallTrace
) = trace.copy(instanceId = mapOriginalToAlias(trace.instanceId))

internal fun AliasMokkeryCollection.aliasTemplate(
    template: CallTemplate
) = template.copy(instanceId = mapOriginalToAlias(template.instanceId))

internal fun MokkeryCollection.withShorterNames(nameShortener: NameShortener): AliasMokkeryCollection {
    val names = ids.mapTo(mutableSetOf()) { it.typeName }
    val shorterNames = nameShortener.shorten(names)
    return withAliasing {
        it.copy(typeName = shorterNames.getValue(it.typeName))
    }
}

internal fun MokkeryCollection.withAliasing(
    aliasCreator: (MokkeryInstanceId) -> MokkeryInstanceId
): AliasMokkeryCollection {
    val from = this
    return object : AliasMokkeryCollection {

        private val mocks = from.scopes.associateBy { aliasCreator(it.instanceId) }
        private val reverseMapping = mocks.entries.associate { it.value.instanceId to it.key }

        override val ids: Set<MokkeryInstanceId> get() = mocks.keys

        override val scopes: Collection<MokkeryInstanceScope>
            get() = from.scopes

        override fun getScopeOrNull(id: MokkeryInstanceId): MokkeryInstanceScope? = mocks[id]

        override fun mapAliasToOriginal(alias: MokkeryInstanceId): MokkeryInstanceId = mocks.getValue(alias).instanceId

        override fun mapOriginalToAlias(original: MokkeryInstanceId): MokkeryInstanceId = reverseMapping.getValue(original)

    }
}
