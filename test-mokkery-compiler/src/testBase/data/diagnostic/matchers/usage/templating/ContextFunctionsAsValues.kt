import dev.mokkery.annotations.Matcher
import dev.mokkery.every
import dev.mokkery.matcher.MokkeryMatcherScope
import dev.mokkery.matcher.any
import dev.mokkery.matcher.gt
import dev.mokkery.matcher.logical.not
import dev.mokkery.mock
import dev.mokkery.templating.ctx
import dev.mokkery.templating.ext

interface Foo {
    fun foo(arg: Int): Int
    fun fooN(arg: Int?): Int?
    fun Int.extFoo(): Int
}

context(@Matcher matcher: Int)
fun MokkeryMatcherScope.notInContext(): Int = not(matcher)

fun legal(flag: Boolean, nullable: Foo?, nullableInt: Int?) {
    val mock = mock<Foo>()
    every { mock.foo(1.ext { this }) }
    every { mock.foo(gt(1).ext { this }) }
    every { mock.foo(ctx(gt(1)) { notInContext() }) }
    every {
        val matcher = ctx(gt(1)) { notInContext() }
        mock.foo(matcher)
    }
    every { mock.foo(if (flag) ctx(gt(1)) { notInContext() } else 1) }
    every {
        val matcher = when {
            flag -> ctx(gt(1)) { notInContext() }
            else -> 1
        }
        mock.foo(matcher)
    }
    every { mock.foo(ctx(1) { 2 } + 1) }
    every { mock.ext { any<Int>().extFoo() } }
    every { ctx(any<Int>()) { mock.foo(notInContext()) } }
    every { nullable?.ext { any<Int>().extFoo() } }
    every { mock.foo(try { ctx(1) { 2 } } finally {}) }
    every {
        var matcher = ctx(1) { val m = gt(1); m }
        matcher = gt(2)
        mock.foo(matcher)
    }
    every { mock.foo(ctx(1) { if (flag) return@ctx gt(1); 2 }) }
    every { mock.fooN(nullableInt?.ext { this }) }
    every { ctx(gt(1)) { mock.foo(contextOf<Int>()) } }
    every { mock.foo(try { ctx(gt(1)) { notInContext() } } catch (e: Exception) { 1 }) }
}

fun illegal(flag: Boolean) {
    val mock = mock<Foo>()
    every { mock.foo(<!ILLEGAL_MEMBER_ACCESS_ON_MATCHER!>ctx(gt(1)) { notInContext() }<!> + 1) }
    every { mock.foo(<!ILLEGAL_MEMBER_ACCESS_ON_MATCHER!>gt(1).ext { this }<!> + 1) }
    every { mock.foo(if (<!ILLEGAL_MEMBER_ACCESS_ON_MATCHER!>ctx(gt(1)) { notInContext() }<!> > 0) 1 else 2) }
    every { mock.foo(<!ILLEGAL_MEMBER_ACCESS_ON_MATCHER!>ctx(gt(1)) { notInContext() }<!>.inc()) }
    every { mock.foo("${<!ILLEGAL_MEMBER_ACCESS_ON_MATCHER!>ctx(gt(1)) { notInContext() }<!>}".length) }
    every {
        when (<!ILLEGAL_MATCHER_IN_WHEN_SUBJECT!>ctx(gt(1)) { notInContext() }<!>) {
            else -> mock.foo(1)
        }
    }
    every { mock.foo(<!ILLEGAL_MEMBER_ACCESS_ON_MATCHER!>ctx(any<Int>()) { ctx(gt(1)) { notInContext() } }<!>.inc()) }
    every { mock.foo(<!ILLEGAL_MEMBER_ACCESS_ON_MATCHER!>ctx(1) { val m = gt(1); m }<!> + 1) }
    every { mock.foo(<!ILLEGAL_MEMBER_ACCESS_ON_MATCHER!>gt(1).ext { val receiver = this; receiver }<!> + 1) }
    every { mock.foo((<!ILLEGAL_MEMBER_ACCESS_ON_MATCHER!>if (flag) { val m = gt(1); m } else 1<!>) + 1) }
    every { mock.foo(<!ILLEGAL_MEMBER_ACCESS_ON_MATCHER!>ctx(1) { if (flag) return@ctx gt(1); 2 }<!> + 1) }
    every { <!ILLEGAL_OPERATOR_USAGE!>any<Int?>()<!>?.ext { mock.fooN(this) } }
    every { mock.fooN(<!ILLEGAL_OPERATOR_USAGE!>any<Int?>()<!>?.ext { this }) }
    every { ctx(gt(1)) { mock.foo(<!ILLEGAL_MEMBER_ACCESS_ON_MATCHER!>contextOf<Int>()<!> + 1) } }
}
