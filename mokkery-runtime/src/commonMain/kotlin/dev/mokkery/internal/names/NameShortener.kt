package dev.mokkery.internal.names

internal interface NameShortener {

    fun shorten(names: Set<String>): Map<String, String>

    companion object {

        val default = ReverseDomainNameShortener.withTypeArgumentsSupport()
    }
}

internal object ReverseDomainNameShortener : NameShortener {

    override fun shorten(names: Set<String>): Map<String, String> {
        val splitNames = names.associateWith { it.split(".") }
        return names.associateWith { shortenName(it, splitNames) }
    }

    private fun shortenName(name: String, splitNames: Map<String, List<String>>): String {
        val parts = splitNames.getValue(name)
        for (i in 1..<parts.size) {
            val shorter = parts.takeLast(i)
            val isUnique = splitNames.all { (key, value) -> key == name || value.takeLast(i) != shorter }
            if (isUnique) return shorter.joinToString(".")
        }
        return name
    }
}
