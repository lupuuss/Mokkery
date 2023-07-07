package dev.mokkery.answering

import dev.mokkery.annotations.DelicateMokkeryApi

/**
 * Scope for defining an answer for a function call.
 */
public interface AnsweringScope<T> {

    /**
     * Registers given [answer] for a function call. Direct usage requires care, because it allows to register
     * suspending answer for non-suspend function.
     */
    @DelicateMokkeryApi
    public fun answers(answer: Answer<T>)
}

/**
 * Marker interface for defining an answer for non-suspend function call.
 */
public interface SuspendAnsweringScope<T> : AnsweringScope<T>

/**
 * Marker interface for defining an answer for suspend function call.
 */
public interface BlockingAnsweringScope<T> : AnsweringScope<T>

