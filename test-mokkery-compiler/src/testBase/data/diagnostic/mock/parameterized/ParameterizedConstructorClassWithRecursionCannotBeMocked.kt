import dev.mokkery.mock
import dev.mokkery.spy
import dev.mokkery.mockMany
import kotlin.CharSequence

abstract class AbstractToStub(p0: AbstractRecursion, p1: Int)
abstract class AbstractRecursion(p0: AbstractToStub)

open class OpenToStub(p0: OpenSubNode, p1: Int)
open class OpenSubNode(p0: OpenRecursion)
open class OpenRecursion(p0: OpenToStub)

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

