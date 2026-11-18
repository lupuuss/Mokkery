// STUBS_ALLOW_CLASS_INHERITANCE

import dev.mokkery.mock
import dev.mokkery.spy
import dev.mokkery.mockMany
import kotlin.CharSequence

abstract class AbstractToStub(p0: AbstractParam, p1: OpenParam, p2: FinalParam, p3: Int)

open class OpenToStub(p0: AbstractParam, p1: OpenParam, p2: FinalParam, p3: Int)

abstract class AbstractParam(p0: Int)
open class OpenParam(p0: Int)
class FinalParam(p0: Int)

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

