import dev.mokkery.mock
import dev.mokkery.spy
import dev.mokkery.mockMany
import kotlin.CharSequence

sealed class SealedClass
sealed interface SealedInterface

fun testClass(i: SealedClass) {
    <!SEALED_TYPE_CANNOT_BE_INTERCEPTED!>spy<!>(i)
    mock<<!SEALED_TYPE_CANNOT_BE_INTERCEPTED!>SealedClass<!>>()
    mockMany<<!SEALED_TYPE_CANNOT_BE_INTERCEPTED!>SealedClass<!>, CharSequence>()
}

fun testInterface(i: SealedInterface) {
    <!SEALED_TYPE_CANNOT_BE_INTERCEPTED!>spy<!>(i)
    mock<<!SEALED_TYPE_CANNOT_BE_INTERCEPTED!>SealedInterface<!>>()
    mockMany<<!SEALED_TYPE_CANNOT_BE_INTERCEPTED!>SealedInterface<!>, CharSequence>()
}
