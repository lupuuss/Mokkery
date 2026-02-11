package dev.mokkery.internal.serialization.compiler.core

internal fun <T> List<T>.removePrefix(vararg prefix: T): List<T> {
    if (!startsWith(*prefix)) return this
    return subList(prefix.size, size)
}

internal fun <T> List<T>.removeSuffix(vararg suffix: T): List<T> {
    if (!endsWith(*suffix)) return this
    return subList(0, size - suffix.size)
}

internal fun <T> List<T>.startsWith(vararg prefix: T): Boolean {
    prefix.forEachIndexed { index, it ->
        if (getOrNull(index) != it) return false
    }
    return true
}

internal fun <T> List<T>.endsWith(vararg prefix: T): Boolean {
    val base = size - prefix.size
    if (base < 0) return false
    prefix.forEachIndexed { index, it ->
        if (getOrNull(base + index) != it) return false
    }
    return true
}

internal fun <T> List<T>.splitBy(delimiter: T): List<List<T>> {
    if (this.isEmpty()) return emptyList()
    val splits = mutableListOf<MutableList<T>>(mutableListOf())
    forEach {
        when (it) {
            delimiter -> splits.add(mutableListOf())
            else -> splits.last().add(it)
        }
    }
    return splits
}
