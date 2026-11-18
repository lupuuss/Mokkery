import dev.mokkery.spy

interface Base {

    fun call(): Int
}

fun test() {
    val anonymous = object : Base {
        override fun call(): Int = 1
    }
    <!ANONYMOUS_TYPE_CANNOT_BE_INTERCEPTED!>spy<!>(anonymous)
}
