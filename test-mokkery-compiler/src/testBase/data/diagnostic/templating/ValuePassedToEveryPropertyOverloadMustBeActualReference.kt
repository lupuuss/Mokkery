import dev.mokkery.every
import dev.mokkery.answering.returns
import dev.mokkery.mock

interface Foo {
    val readOnly: Int

    var mutable: Int
}

fun main() {
    val foo = mock<Foo>()
    val readOnlyRef = foo::readOnly
    val mutableRef = foo::mutable
    every(<!PROPERTY_PARAM_MUST_BE_REFERENCE!>readOnlyRef<!>) returns 1
    every(<!PROPERTY_PARAM_MUST_BE_REFERENCE!>mutableRef<!>)
    every(<!PROPERTY_PARAM_MUST_BE_REFERENCE!>mutableRef<!>) { }
}
