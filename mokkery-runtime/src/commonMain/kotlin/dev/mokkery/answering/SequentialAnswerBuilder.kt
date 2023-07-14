package dev.mokkery.answering

/**
 * Marker interface for sequential answer builder for any function
 */
public interface SequentialAnswerBuilder<T> : AnsweringScope<T>


/**
 * Marker interface for sequential answer builder for suspend function
 */
public interface SuspendSequentialAnswerBuilder<T> : SuspendAnsweringScope<T>, SequentialAnswerBuilder<T>


/**
 * Marker interface for sequential answer builder for non-suspend function
 */
public interface BlockingSequentialAnswerBuilder<T> : BlockingAnsweringScope<T>, SequentialAnswerBuilder<T>
