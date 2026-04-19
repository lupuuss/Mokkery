package dev.mokkery.test

import dev.mokkery.mockable.annotations.Mockable

class NotMockableClass

@Mockable
class MockableClass(
    private val arg: NotMockableClass,
) {
    init {
        error("FAILED!")
    }

    fun call(): Int = 1
}

@Mockable
open class OpenMockableClass {
    init {
        error("FAILED!")
    }

    fun baseCall(): Int = 1
}

@Mockable
class MockableDependingClass : OpenMockableClass() {
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
class CustomMockableDependingOnStandardClass : OpenMockableClass() {
    fun call(): Int = 1
}
