package dev.mokkery.debug

internal interface HierarchicalStringBuilder {
    fun startSection(headline: String)

    fun line(line: String)

    fun endSection()

    fun build(): String
}

internal fun buildHierarchicalString(builder: HierarchicalStringBuilder.() -> Unit): String {
    val renderer = JsonLikeStringBuilder()
    return renderer.apply(builder).build()
}


internal fun HierarchicalStringBuilder.addItem(builder: StringBuilder.() -> Unit) {
    line(StringBuilder().apply(builder).toString())
}

internal fun HierarchicalStringBuilder.value(key: String, value: String) {
    line("$key = $value")
}

internal fun HierarchicalStringBuilder.section(headline: String, renderer: HierarchicalStringBuilder.() -> Unit) {
    startSection(headline)
    renderer()
    endSection()
}

private class JsonLikeStringBuilder : HierarchicalStringBuilder {
    private val builder = StringBuilder()
    private var indent = ""
    private var nestedSectionCounter = 0

    override fun startSection(headline: String) {
        line("$headline {")
        nestedSectionCounter++
        indent = buildIndent(nestedSectionCounter)
    }

    override fun line(line: String) {
        builder.append(indent)
        builder.append(line)
        builder.appendLine()
    }

    override fun endSection() {
        nestedSectionCounter--
        indent = buildIndent(nestedSectionCounter)
        line("}")
    }

    override fun build(): String = builder.toString()

    private fun buildIndent(i: Int) = generateSequence { "\t" }.take(i).joinToString(separator = "")
}
