package dev.mokkery.debug

internal interface HierarchicalStringBuilder {
    fun startSection(headline: String)

    fun addItem(item: String)

    fun endSection()

    fun build(): String
}

internal fun buildHierarchicalString(builder: HierarchicalStringBuilder.() -> Unit): String {
    val renderer = JsonLikeStringBuilder()
    return renderer.apply(builder).build()
}


internal fun HierarchicalStringBuilder.addItem(builder: StringBuilder.() -> Unit) {
    addItem(StringBuilder().apply(builder).toString())
}

internal fun HierarchicalStringBuilder.addValue(key: String, value: String) {
    addItem("$key = $value")
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
        addItem("$headline {")
        nestedSectionCounter++
        indent = buildIndent(nestedSectionCounter)
    }

    override fun addItem(item: String) {
        builder.append(indent)
        builder.append(item)
        builder.appendLine()
    }

    override fun endSection() {
        nestedSectionCounter--
        indent = buildIndent(nestedSectionCounter)
        addItem("}")
    }

    override fun build(): String = builder.toString()

    private fun buildIndent(i: Int) = generateSequence { "\t" }.take(i).joinToString(separator = "")
}
