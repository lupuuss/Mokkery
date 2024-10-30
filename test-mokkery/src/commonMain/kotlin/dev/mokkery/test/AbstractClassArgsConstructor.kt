package dev.mokkery.test

abstract class AbstractClassArgsConstructor(i: InterfaceParam, s: SealedParam, f: FinalParam) {

    abstract fun call(input: Int): Int

    interface InterfaceParam
    sealed class SealedParam
    sealed class FinalParam
}
