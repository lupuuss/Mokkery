import dev.mokkery.mock
import dev.mokkery.every
import dev.mokkery.verify
import dev.mokkery.matcher.any

interface Foo {
    fun foo(arg: Int): Int
}

fun mainDenied() {
    val mock = mock<Foo>()
    every {
        val matcher = <!ILLEGAL_TRY_CATCH!>try {
            any<Int>()
        } catch (e: Exception) {
            0
        }<!>
        mock.foo(matcher)
    }
    verify {
        val matcher = <!ILLEGAL_TRY_CATCH!>try {
            any<Int>()
        } catch (e: Exception) {
            0
        }<!>
        mock.foo(matcher)
    }
}

fun mainAllowed() {
    val mock = mock<Foo>()
    every {
        val matcher = try {
            any<Int>()
            1
        } catch (e: Exception) {
            0
        }
        mock.foo(matcher)
    }
    verify {
        val matcher = try {
            any<Int>()
            1
        } catch (e: Exception) {
            0
        }
        mock.foo(matcher)
    }
}
