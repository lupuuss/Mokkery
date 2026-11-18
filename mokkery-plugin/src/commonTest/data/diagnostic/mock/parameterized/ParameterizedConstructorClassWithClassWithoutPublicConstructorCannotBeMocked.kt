import dev.mokkery.mock
import dev.mokkery.spy
import dev.mokkery.mockMany
import kotlin.CharSequence

abstract class AbstractToStub(p: PrivateConstructor)

open class OpenToStub(p: PrivateConstructor)

class PrivateConstructor private constructor()

fun test(i: AbstractToStub) {
    <!NO_CONSTRUCTOR_TO_STUB!>spy<!>(i)
    mock<<!NO_CONSTRUCTOR_TO_STUB!>AbstractToStub<!>>()
    mockMany<<!NO_CONSTRUCTOR_TO_STUB!>AbstractToStub<!>, CharSequence>()
}

fun test(i: OpenToStub) {
    <!NO_CONSTRUCTOR_TO_STUB!>spy<!>(i)
    mock<<!NO_CONSTRUCTOR_TO_STUB!>OpenToStub<!>>()
    mockMany<<!NO_CONSTRUCTOR_TO_STUB!>OpenToStub<!>, CharSequence>()
}

