package dev.mokkery.plugin

private const val FNV_OFFSET_BASIS = 0xcbf29ce484222325UL
private const val FNV_PRIME = 0x100000001b3UL
private const val SEPARATOR = '\u0000'

fun fnv1a64(strings: List<String>): Long {
    var hash = FNV_OFFSET_BASIS
    strings.forEach { string -> hash = hash.fnv1a64(string).fnv1a64(SEPARATOR) }
    return hash.toLong()
}

fun fnv1a64(string: String): Long = FNV_OFFSET_BASIS.fnv1a64(string).toLong()

private fun ULong.fnv1a64(string: String): ULong {
    var hash = this
    string.forEach { hash = hash.fnv1a64(it) }
    return hash
}

private fun ULong.fnv1a64(char: Char): ULong = (this xor char.code.toULong()) * FNV_PRIME
