import dev.mokkery.mock
import dev.mokkery.spy
import dev.mokkery.mockMany
import kotlin.CharSequence

fun test(i: Int) {
    <!PRIMITIVE_TYPE_CANNOT_BE_INTERCEPTED!>spy<!>(i)
    mock<<!PRIMITIVE_TYPE_CANNOT_BE_INTERCEPTED!>Int<!>>()
    mockMany<<!PRIMITIVE_TYPE_CANNOT_BE_INTERCEPTED!>Int<!>, CharSequence>()
}
