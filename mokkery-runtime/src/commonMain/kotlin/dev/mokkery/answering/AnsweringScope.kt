package dev.mokkery.answering

import dev.mokkery.annotations.DelicateMokkeryApi

public interface AnsweringScope<T> {

    @DelicateMokkeryApi
    public fun answers(answer: Answer<T>)
}

public interface SuspendAnsweringScope<T> : AnsweringScope<T>

public interface RegularAnsweringScope<T> : AnsweringScope<T>

