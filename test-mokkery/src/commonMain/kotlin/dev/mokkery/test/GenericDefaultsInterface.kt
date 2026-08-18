package dev.mokkery.test

interface GenericDefaultsInterface<T> {

    fun call(value: T, other: T? = null, name: String = "name($value)"): String
}
