package dev.mokkery.test

// no accessible constructor - Mokkery can never supply an instance of it,
// so it may only appear on constructor parameters that have a default value
class NotStubbableParam private constructor(val value: Int) {

    companion object {

        fun create(): NotStubbableParam = NotStubbableParam(1)
    }
}

// callable with no arguments at all - no stubs are required
abstract class AllDefaultsConstructor(val param: NotStubbableParam = NotStubbableParam.create()) {

    abstract fun call(input: Int): Int
}

// `number` has to be stubbed, `param` is left to its default value
abstract class PartialDefaultsConstructor(
    val number: Int,
    val param: NotStubbableParam = NotStubbableParam.create(),
) {

    abstract fun call(input: Int): Int
}

// forces stubbing of `PartialDefaultsParam` itself, which again requires skipping a defaulted parameter
open class PartialDefaultsParam(
    val number: Int,
    val param: NotStubbableParam = NotStubbableParam.create(),
)

abstract class NestedDefaultsConstructor(val param: PartialDefaultsParam) {

    abstract fun call(input: Int): Int
}
