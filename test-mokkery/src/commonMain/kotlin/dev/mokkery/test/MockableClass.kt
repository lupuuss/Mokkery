package dev.mokkery.test

import dev.mokkery.mockable.annotations.Mockable

class NotMockableClass

@Mockable
class MockableClass private constructor(
    arg: NotMockableClass,
) {
    init {
        error("FAILED!")
    }

    fun call(): Int = 1
}

@Mockable
abstract class AbstractMockableClass {
    init {
        error("FAILED!")
    }

    fun baseCall(): Int = 1
}

@Mockable
class MockableDependingClass : AbstractMockableClass() {
    fun call(): Int = 1
}

@CustomMockable
open class CustomMockableClass(
    private val arg: NotMockableClass,
) {
    init {
        error("FAILED!")
    }

    fun call(): Int = 1
}

@Mockable
open class OpenCustomMockableClass {
    init {
        error("FAILED!")
    }

    fun baseCall(): Int = 1
}

@Mockable
class CustomMockableDependingOnCustomClass : OpenCustomMockableClass() {
    fun call(): Int = 1
}

@Mockable
class CustomMockableDependingOnStandardClass : AbstractMockableClass() {
    fun call(): Int = 1
}
