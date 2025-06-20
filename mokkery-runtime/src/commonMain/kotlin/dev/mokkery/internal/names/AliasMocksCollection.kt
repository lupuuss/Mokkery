package dev.mokkery.internal.names

import dev.mokkery.internal.MockId
import dev.mokkery.internal.MocksCollection
import dev.mokkery.internal.MokkeryInstanceScope
import dev.mokkery.internal.calls.CallTemplate
import dev.mokkery.internal.calls.CallTrace
import dev.mokkery.internal.mockId

internal interface AliasMocksCollection : MocksCollection {

    fun mapAliasToOriginal(alias: MockId): MockId

    fun mapOriginalToAlias(original: MockId): MockId
}

internal fun AliasMocksCollection.aliasTraces(
    traces: List<CallTrace>
): List<CallTrace> = traces.map { aliasTrace(it) }

internal fun AliasMocksCollection.aliasTemplates(
    templates: List<CallTemplate>
): List<CallTemplate> = templates.map { aliasTemplate(it) }

internal fun AliasMocksCollection.aliasTrace(
    trace: CallTrace
) = trace.copy(mockId = mapOriginalToAlias(trace.mockId))

internal fun AliasMocksCollection.aliasTemplate(
    template: CallTemplate
) = template.copy(mockId = mapOriginalToAlias(template.mockId))

internal fun MocksCollection.withShorterNames(nameShortener: NameShortener): AliasMocksCollection {
    val names = ids.mapTo(mutableSetOf()) { it.typeName }
    val shorterNames = nameShortener.shorten(names)
    return withAliasing {
        it.copy(typeName = shorterNames.getValue(it.typeName))
    }
}

internal fun MocksCollection.withAliasing(
    aliasCreator: (MockId) -> MockId
): AliasMocksCollection {
    val from = this
    return object : AliasMocksCollection {

        private val mocks = from.scopes.associateBy { aliasCreator(it.mockId) }
        private val reverseMapping = mocks.entries.associate { it.value.mockId to it.key }

        override val ids: Set<MockId> get() = mocks.keys

        override val scopes: Collection<MokkeryInstanceScope>
            get() = from.scopes

        override fun getScopeOrNull(id: MockId): MokkeryInstanceScope? = mocks[id]

        override fun mapAliasToOriginal(alias: MockId): MockId = mocks.getValue(alias).mockId

        override fun mapOriginalToAlias(original: MockId): MockId = reverseMapping.getValue(original)

    }
}
