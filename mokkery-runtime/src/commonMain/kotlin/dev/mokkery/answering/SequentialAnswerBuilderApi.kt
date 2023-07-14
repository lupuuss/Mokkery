package dev.mokkery.answering

import dev.mokkery.internal.answering.unifiedSequentially
import dev.mokkery.internal.answering.unifiedRepeat


public infix fun <T> BlockingAnsweringScope<T>.sequentially(block: BlockingSequentialAnswerBuilder<T>.() -> Unit) {
    unifiedSequentially(block)
}

public infix fun <T> SuspendAnsweringScope<T>.sequentially(block: SuspendSequentialAnswerBuilder<T>.() -> Unit) {
    unifiedSequentially(block)
}


public fun <T> SuspendSequentialAnswerBuilder<T>.repeat(block: SuspendSequentialAnswerBuilder<T>.() -> Unit): Nothing {
    unifiedRepeat(block)
}

public fun <T> BlockingSequentialAnswerBuilder<T>.repeat(block: BlockingSequentialAnswerBuilder<T>.() -> Unit): Nothing {
    unifiedRepeat(block)
}
