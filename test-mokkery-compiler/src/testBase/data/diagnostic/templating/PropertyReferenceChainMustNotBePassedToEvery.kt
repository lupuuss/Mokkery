import dev.mokkery.every
import dev.mokkery.answering.returns
import dev.mokkery.mock

interface Foo {
    val readOnly: Int

    var mutable: Int
}

fun main() {
    val foo = mock<Foo>()
    every(<!PROPERTY_REFERENCE_CHAIN_NOT_ALLOWED!>foo::readOnly::name<!>) returns ""
    every(<!PROPERTY_REFERENCE_CHAIN_NOT_ALLOWED!>foo::mutable::name<!>) returns ""
}
