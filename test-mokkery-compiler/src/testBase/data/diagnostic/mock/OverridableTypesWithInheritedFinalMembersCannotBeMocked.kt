import dev.mokkery.mock
import dev.mokkery.spy
import dev.mokkery.mockMany
import kotlin.CharSequence

abstract class GrandParent {
    fun finalFromGrandParent() = Unit
}

abstract class Parent : GrandParent()

abstract class Child : Parent()

fun testDeeplyInherited(i: Child) {
    <!FINAL_MEMBERS_TYPE_CANNOT_BE_INTERCEPTED!>spy<!>(i)
    mock<<!FINAL_MEMBERS_TYPE_CANNOT_BE_INTERCEPTED!>Child<!>>()
    mockMany<<!FINAL_MEMBERS_TYPE_CANNOT_BE_INTERCEPTED!>Child<!>, CharSequence>()
}

interface OpenGrandParent {
    fun openFromGrandParent() = Unit
}

interface OpenParent : OpenGrandParent

abstract class OpenChild : OpenParent

fun testDeeplyInheritedWithoutFinals() {
    mock<OpenChild>()
}
