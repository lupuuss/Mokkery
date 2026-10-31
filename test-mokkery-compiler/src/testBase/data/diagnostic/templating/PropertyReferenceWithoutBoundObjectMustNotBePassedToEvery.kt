import dev.mokkery.every
import dev.mokkery.answering.returns

interface Foo {
    val readOnly: Int

    var mutable: Int
}

fun main() {
    every(<!PROPERTY_REFERENCE_NOT_BOUND!>Foo::readOnly<!>) returns 1
    every(<!PROPERTY_REFERENCE_NOT_BOUND!>Foo::mutable<!>)
    every(<!PROPERTY_REFERENCE_NOT_BOUND!>Foo::mutable<!>) { }
}
