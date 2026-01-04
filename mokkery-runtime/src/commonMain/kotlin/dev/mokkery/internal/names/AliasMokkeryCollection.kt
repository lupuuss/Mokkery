package dev.mokkery.internal.names

import dev.mokkery.MokkeryInstanceScope
import dev.mokkery.internal.MokkeryCollection
import dev.mokkery.internal.MokkeryInstanceId
import dev.mokkery.internal.instanceId

internal interface AliasMokkeryCollection : MokkeryCollection {

    fun mapAliasToOriginal(alias: MokkeryInstanceId): MokkeryInstanceId

    fun mapOriginalToAlias(original: MokkeryInstanceId): MokkeryInstanceId
}

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

        override val ids: Set<MokkeryInstanceId>
            get() = mocks.keys

        override val scopes: Collection<MokkeryInstanceScope>
            get() = from.scopes

        override fun getScopeOrNull(id: MokkeryInstanceId): MokkeryInstanceScope? = mocks[id]

        override fun mapAliasToOriginal(alias: MokkeryInstanceId): MokkeryInstanceId = mocks.getValue(alias).instanceId

        override fun mapOriginalToAlias(original: MokkeryInstanceId): MokkeryInstanceId = reverseMapping.getValue(original)
    }
}
