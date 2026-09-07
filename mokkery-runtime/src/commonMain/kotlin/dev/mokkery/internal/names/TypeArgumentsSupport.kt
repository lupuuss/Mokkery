package dev.mokkery.internal.names


internal fun NameShortener.withTypeArgumentsSupport(): NameShortener {
    return TypeParametersSupportNameShortener(this)
}

private class TypeParametersSupportNameShortener(private val baseShortener: NameShortener) : NameShortener {

    override fun shorten(names: Set<String>): Map<String, String> {
        names.shortenSingleNotParametrizedOrNull()?.let { return it }
        val mapping = baseShortener.shorten(names.flatMapTo(mutableSetOf(), ::extractNames))
        val longestNamesFirst = mapping.keys.sortedByDescending(String::length)
        return names.associateWith { name ->
            if (name.contains("<")) name
                .replaceIndividualLongNamesWithIndexes(longestNamesFirst)
                .replaceIndexesWithShortNames(longestNamesFirst, mapping)
            else mapping.getValue(name)
        }
    }

    private fun String.replaceIndividualLongNamesWithIndexes(names: List<String>): String = names
        .foldIndexed(this) { index, acc, longName ->
            acc.replace(longName, "%$index%")
        }

    private fun String.replaceIndexesWithShortNames(names: List<String>, mapping: Map<String, String>): String {
        return names.foldIndexed(this) { index, acc, longName ->
            acc.replace("%$index%", mapping.getValue(longName))
        }
    }

    private fun extractNames(name: String): Set<String> {
        return when {
            name.contains("<") -> name.replace(">", "").split(", ", "<").toSet()
            else -> setOf(name)
        }
    }

    private fun Set<String>.shortenSingleNotParametrizedOrNull(): Map<String, String>? {
        val single = singleOrNull() ?: return null
        if (single.contains("<")) return null
        return baseShortener.shorten(this)
    }
}
