package dev.mokkery.answering

/**
 * Scope for defining an answer for reads of a read-only property (`val`). All answers for regular function calls
 * are available. It is also used for a `var` whose setter is not accessible.
 */
public interface PropertyAnsweringScope<T> : BlockingAnsweringScope<T>

/**
 * Scope for defining answers for a mutable property (`var`). Answers for each accessor are defined via [getter]
 * and [setter]. [returns], [returnsBy], [throws], [throwsBy], [throwsErrorWith] and [by] define both at once.
 */
public interface MutablePropertyAnsweringScope<T> {

    /**
     * Scope for defining an answer for reads of this property. All answers for regular function calls are available.
     */
    public val getter: BlockingAnsweringScope<T>

    /**
     * Scope for defining an answer for writes to this property. It applies to any written value.
     */
    public val setter: BlockingAnsweringScope<Unit>
}
