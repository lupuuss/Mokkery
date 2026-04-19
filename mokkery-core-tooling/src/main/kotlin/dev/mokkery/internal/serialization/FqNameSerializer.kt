package dev.mokkery.internal.serialization

internal object FqNameSerializer : MokkerySerializer<String> {
    override fun serialize(obj: String): String = obj.also(::validate)

    override fun deserialize(string: String): String = string.also(::validate)

    private fun validate(fqName: String) {
        require(fqName.isNotEmpty()) { "Invalid FQ name - must not be empty" }
        val segments = fqName.split(".")
        var offset = 0
        segments.forEach { segment ->
            if (segment.isEmpty()) fqNameError(fqName, offset, "empty segment")
            val illegalIndex = segment.indexOfFirst { !it.isJavaIdentifierPart() }
                .takeIf { it != -1 }
                ?: if (!segment.first().isJavaIdentifierStart()) 0 else null
            if (illegalIndex != null) {
                val char = segment[illegalIndex]
                val message = if (illegalIndex == 0) "illegal start character '$char'"
                              else "illegal character '$char'"
                fqNameError(fqName, offset + illegalIndex, message)
            }
            offset += segment.length + 1
        }
    }

    private fun fqNameError(fqName: String, position: Int, message: String): Nothing {
        error("Invalid FQ name - $message:\n$fqName\n${" ".repeat(position)}^")
    }
}
