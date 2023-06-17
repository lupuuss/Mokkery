package dev.mokkery.answering

import dev.mokkery.annotations.DelicateMokkeryApi

public sealed interface AnsweringScope<T> {

    @DelicateMokkeryApi
    public fun answers(answer: Answer<T>)
}

public sealed interface SuspendAnsweringScope<T> : AnsweringScope<T>

public sealed interface RegularAnsweringScope<T> : AnsweringScope<T>

