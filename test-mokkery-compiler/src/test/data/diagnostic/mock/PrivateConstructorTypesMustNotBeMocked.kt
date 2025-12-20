import dev.mokkery.mock
import dev.mokkery.spy
import dev.mokkery.mockMany

abstract class AbstractClass private constructor(){
    abstract fun foo()
}

fun testAbstract(i: AbstractClass) {
    <!NO_PUBLIC_CONSTRUCTOR_TYPE_CANNOT_BE_INTERCEPTED!>spy<!>(i)
    mock<<!NO_PUBLIC_CONSTRUCTOR_TYPE_CANNOT_BE_INTERCEPTED!>AbstractClass<!>>()
    mockMany<<!NO_PUBLIC_CONSTRUCTOR_TYPE_CANNOT_BE_INTERCEPTED!>AbstractClass<!>, CharSequence>()
}
