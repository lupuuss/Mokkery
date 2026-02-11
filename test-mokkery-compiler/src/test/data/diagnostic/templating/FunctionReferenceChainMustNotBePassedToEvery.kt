import dev.mokkery.every
import dev.mokkery.everySuspend
import dev.mokkery.answering.returns
import dev.mokkery.mock

interface Foo {

    var property: Int

    fun call(i: Int, d: Double): Int
}

fun main() {
    val foo = mock<Foo>()
    every(<!FUNCTION_REFERENCE_CHAIN_NOT_ALLOWED!>foo::call::hashCode<!>) returns 1
    every(<!FUNCTION_REFERENCE_CHAIN_NOT_ALLOWED!>foo::call::toString::hashCode<!>) returns 1
    every(<!FUNCTION_REFERENCE_CHAIN_NOT_ALLOWED!>foo::property::get::hashCode<!>) returns 1
}
