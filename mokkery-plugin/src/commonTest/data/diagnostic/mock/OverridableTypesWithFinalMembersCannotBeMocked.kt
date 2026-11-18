import dev.mokkery.mock
import dev.mokkery.spy
import dev.mokkery.mockMany
import kotlin.CharSequence

abstract class AbstractClass {
    fun finalMethod() = Unit
}

fun testAbstract(i: AbstractClass) {
    <!FINAL_MEMBERS_TYPE_CANNOT_BE_INTERCEPTED!>spy<!>(i)
    mock<<!FINAL_MEMBERS_TYPE_CANNOT_BE_INTERCEPTED!>AbstractClass<!>>()
    mockMany<<!FINAL_MEMBERS_TYPE_CANNOT_BE_INTERCEPTED!>AbstractClass<!>, CharSequence>()
}

open class OpenClass {
    fun finalMethod() = Unit
}

fun testOpen(i: OpenClass) {
    <!FINAL_MEMBERS_TYPE_CANNOT_BE_INTERCEPTED!>spy<!>(i)
    mock<<!FINAL_MEMBERS_TYPE_CANNOT_BE_INTERCEPTED!>OpenClass<!>>()
    mockMany<<!FINAL_MEMBERS_TYPE_CANNOT_BE_INTERCEPTED!>OpenClass<!>, CharSequence>()
}
