package dev.mokkery.internal.serialization.compiler.core

internal interface PeekStream<out T> {

    val position: Int

    fun peek(): T?

    fun peek(at: Int): T?

    fun consumed()

    fun consumed(n: Int)
}

internal inline fun <T> PeekStream<T>.consumedWhile(block: (T) -> Boolean) {
    while (true) {
        val peek = peek() ?: return
        if (!block(peek)) return
        consumed()
    }
}

internal inline fun <T> PeekStream<T>.collectConsumedWhile(block: (T) -> Boolean): List<T> {
    val results = mutableListOf<T>()
    while (true) {
        val peek = peek() ?: return results
        if (!block(peek)) return results
        else results += peek
        consumed()
    }
}

internal fun <T> List<T>.asPeekStream(): PeekStream<T> = PeekStream(
    size = this::size,
    get = this::getOrNull,
)

internal fun String.asPeekStream(): PeekStream<Char> = PeekStream(
    size = this::length,
    get = this::getOrNull
)

private fun <T : Any> PeekStream(
    size: () -> Int,
    get: (Int) -> T?,
): PeekStream<T> {
    return object : PeekStream<T> {
        private var consumed = 0

        override val position: Int
            get() = consumed

        override fun peek(): T? {
            if (consumed >= size()) return null
            return get(consumed)
        }

        override fun peek(at: Int): T? {
            return get(consumed + at)
        }

        override fun consumed() {
            consumed(1)
        }

        override fun consumed(n: Int) {
            consumed += n
        }
    }
}
