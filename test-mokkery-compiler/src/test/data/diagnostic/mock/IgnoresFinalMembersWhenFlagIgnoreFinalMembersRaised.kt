// IGNORE_FINAL_MEMBERS

import dev.mokkery.mock
import dev.mokkery.spy
import dev.mokkery.mockMany
import kotlin.CharSequence

abstract class AbstractClass {

    inline fun inlineMethod(block: () -> Unit) = block()

    fun finalMethod() = Unit
}

fun testAbstract(i: AbstractClass) {
    spy(i)
    mock<AbstractClass>()
    mockMany<AbstractClass, CharSequence>()
}

open class OpenClass {

    inline fun inlineMethod(block: () -> Unit) = block()

    fun finalMethod() = Unit
}

fun testOpen(i: OpenClass) {
    spy(i)
    mock<OpenClass>()
    mockMany<OpenClass, CharSequence>()
}
