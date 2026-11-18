import dev.mokkery.every
import dev.mokkery.everySuspend
import dev.mokkery.answering.returns
import dev.mokkery.mock

interface Foo {
    var property: Int
}

fun main() {
    val foo = mock<Foo>()
    every(<!PROPERTY_FUNCTION_REFERENCE_MUST_BE_ACCESSOR!>foo::property::hashCode<!>) returns 1
}
