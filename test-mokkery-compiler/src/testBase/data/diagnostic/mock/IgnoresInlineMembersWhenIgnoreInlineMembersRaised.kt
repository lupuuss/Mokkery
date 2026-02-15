// IGNORE_INLINE_MEMBERS

import dev.mokkery.mock
import dev.mokkery.spy
import dev.mokkery.mockMany
import kotlin.CharSequence

abstract class AbstractClass {

    inline fun inlineMethod(block: () -> Unit) = block()
}

fun testAbstract(i: AbstractClass) {
    spy(i)
    mock<AbstractClass>()
    mockMany<AbstractClass, CharSequence>()
}

open class OpenClass {

    inline fun inlineMethod(block: () -> Unit) = block()
}

fun testOpen(i: OpenClass) {
    spy(i)
    mock<OpenClass>()
    mockMany<OpenClass, CharSequence>()
}
