import dev.mokkery.mock
import dev.mokkery.spy
import dev.mokkery.mockMany
import kotlin.CharSequence

class FinalClass

fun test(i: FinalClass) {
    <!FINAL_TYPE_CANNOT_BE_INTERCEPTED!>spy<!>(i)
    mock<<!FINAL_TYPE_CANNOT_BE_INTERCEPTED!>FinalClass<!>>()
    mockMany<<!FINAL_TYPE_CANNOT_BE_INTERCEPTED!>FinalClass<!>, CharSequence>()
}
