package dev.mokkery.test

abstract class AbstractGenericClass<Type> {

    abstract fun call(value: Type): Type

    abstract fun <T : Type> callGeneric(value: T): T
}

