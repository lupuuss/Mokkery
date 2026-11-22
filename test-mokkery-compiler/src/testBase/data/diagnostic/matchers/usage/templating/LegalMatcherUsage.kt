import dev.mokkery.annotations.Matcher
import dev.mokkery.every
import dev.mokkery.everySuspend
import dev.mokkery.matcher.MokkeryMatcherScope
import dev.mokkery.matcher.any
import dev.mokkery.matcher.gt
import dev.mokkery.matcher.logical.not
import dev.mokkery.matcher.logical.or
import dev.mokkery.mock
import dev.mokkery.templating.ctx
import dev.mokkery.templating.ext
import dev.mokkery.verify

interface Foo {
    fun foo(arg: Int): Int
    fun bar(arg: Int?): Int?
    fun vararg(vararg args: Int): Int
    fun list(args: List<Int>): Int
    fun Int.extFun(other: Int): Int
    context(c: String) fun ctxFun(other: Int): Int
    var prop: Int
    val nested: Foo
    suspend fun sus(arg: Int): Int
    operator fun get(i: Int): Int
    operator fun set(i: Int, v: Int)
    operator fun plus(i: Int): Int
    operator fun contains(i: Int): Boolean
    operator fun invoke(i: Int): Int
    fun text(arg: String): Int
    val Int.extProp: Int
    context(c: String) val ctxProp: Int
}

fun test(flag: Boolean, value: Int) {
    val mock = mock<Foo>()
    every { mock.foo(any()) }
    every { mock.bar(null) }
    every { mock.foo(value + 1) }
    every { mock.foo(listOf(1).map { it + value }.first()) }
    every { mock.foo(if (flag) any() else 1) }
    every { mock.foo(when { flag -> gt(1); else -> not(2) }) }
    every { mock.foo(or(1, any())) }
    every {
        val matcher = any<Int>()
        mock.foo(matcher)
    }
    every {
        var matcher = any<Int>()
        matcher = gt(2)
        mock.foo(matcher)
    }
    every { mock.vararg(*intArrayOf(any(), 2)) }
    every { mock.vararg(1, *any()) }
    every { mock.list(any()) }
    every { mock.ext { any<Int>().extFun(any()) } }
    every { ctx("a") { mock.ctxFun(any()) } }
    every { mock.prop = any() }
    every { mock.nested.foo(any()) }
    every { mock.nested.prop = any() }
    every { mock[any()] }
    every { mock[any()] = any() }
    every { mock + any() }
    every { any<Int>() in mock }
    every { mock(any()) }
    everySuspend { mock.sus(any()) }
    verify { mock.foo(any()) }
}

class FinalFoo {
    var prop: Int = 0

    fun foo(arg: Int): Int = arg
}

fun otherLegalUsages(value: Int?, pair: Pair<Int, Int>, callback: (Int) -> Int) {
    val mock = mock<Foo>()
    val finalFoo = FinalFoo()
    every { mock.ext { any<Int>().extProp } }
    every { ctx("a") { mock.ctxProp } }
    every {
        val matcher = any<Int>()
        mock.ext { matcher.extFun(matcher) }
    }
    every { finalFoo.prop = 1 }
    every { finalFoo.foo(1) }
    every { mock.foo(value ?: any()) }
    every { mock.foo(value!!) }
    every { mock.text("$value") }
    every {
        val (first, second) = pair
        mock.foo(callback(first) + second)
    }
    every { mock.foo(listOf("a").map(String::length).first()) }
}

fun expressionShapes(flag: Boolean) {
    val mock = mock<Foo>()
    val nullableMock: Foo? = mock
    every { mock.foo(arg = any()) }
    every { mock.foo((any())) }
    every { mock.foo(label@ any()) }
    every { mock.foo(or(second = gt(1), first = 2)) }
    every {
        mock.foo(
            when (flag) {
                true -> any()
                false -> 1
            }
        )
    }
    every { nullableMock?.foo(any()) }
}

context(value: Int)
fun regularWithContext(): Int = value

context(@Matcher matcher: Int)
fun MokkeryMatcherScope.notInContext(): Int = not(matcher)

fun ctxValues() {
    val mock = mock<Foo>()
    every { ctx(any<Int>()) { mock.foo(notInContext()) } }
    every { ctx(1) { mock.foo(notInContext()) } }
    every { ctx(1) { mock.foo(regularWithContext()) } }
    every { ctx("a", any<Int>()) { mock.ctxFun(notInContext()) } }
}

context(scope: MokkeryMatcherScope)
fun @receiver:Matcher Int.notReceiver(): Int = scope.not(this)

fun extValues() {
    val mock = mock<Foo>()
    every { any<Int>().ext { mock.foo(notReceiver()) } }
    every { 1.ext { mock.foo(notReceiver()) } }
}

fun tryCatchValues(flag: Boolean) {
    val mock = mock<Foo>()
    every { mock.foo(try { any() } catch (e: Exception) { 0 }) }
    every {
        val matcher = try { any<Int>() } finally { }
        mock.foo(not(matcher))
    }
    every {
        mock.foo(ctx(1) {
            try {
                if (flag) return@ctx any()
                2
            } catch (e: Exception) {
                return@ctx gt(1)
            }
        })
    }
}

class Holder<T> {
    fun pass(value: T): T = value
}

fun substitutedScopeValues(mapper: Mapper<MokkeryMatcherScope, Int>, provide: MokkeryMatcherScope.() -> Int) {
    val mock = mock<Foo>()
    every { mock.foo(mapper.map(this) + 1) }
    every { mock.foo(provide() + 1) }
    every { mock.foo(Holder<MokkeryMatcherScope>().pass(this).gt(3)) }
}

fun interface Mapper<T, R> {
    fun map(value: T): R
}
