package dev.mokkery.internal.names


internal fun NameShortener.withTypeArgumentsSupport(): NameShortener {
    return TypeParametersSupportNameShortener(this)
}

private class TypeParametersSupportNameShortener(private val baseShortener: NameShortener) : NameShortener {

    override fun shorten(names: Set<String>): Map<String, String> {
        val mapping = baseShortener.shorten(names.flatMapTo(mutableSetOf(), ::extractNames))
        return names.associateWith {
            if (it.contains("<")) mapping.entries.fold(it) { longName, (key, value) -> longName.replace(key, value) }
            else mapping.getValue(it)
        }
    }

    private fun extractNames(name: String): Set<String> {
        return when {
            name.contains("<") -> name.replace(">", "").split(", ", "<").toSet()
            else -> setOf(name)
        }
    }
}