package dev.mokkery.answering

import dev.drewhamilton.poko.Poko

/**
 * Contains arguments passed to mocked method. If method has extension receiver it is passed at the start of this list.
 *
 * It is possible to apply destructuring declaration with convenient cast using generic [component1], [component2] etc.
 *
 * ```kotlin
 * ever { dependency.foo(1) } calls { (i: Int) ->  }
 * ```
 */
@Poko
public class CallArgs(public val args: List<Any?>) {

    /**
     * Returns argument with [index] from [args] and expects that it is an instance of type [T].
     */
    public inline fun <reified T> arg(index: Int): T = args[index] as T

    public inline operator fun <reified T> component1(): T = arg(0)
    public inline operator fun <reified T> component2(): T = arg(1)
    public inline operator fun <reified T> component3(): T = arg(2)
    public inline operator fun <reified T> component4(): T = arg(3)
    public inline operator fun <reified T> component5(): T = arg(4)
    public inline operator fun <reified T> component6(): T = arg(5)
    public inline operator fun <reified T> component7(): T = arg(6)
}
