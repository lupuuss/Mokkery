import dev.mokkery.every
import dev.mokkery.everySuspend
import dev.mokkery.verify
import dev.mokkery.verifySuspend
import dev.mokkery.templating.MokkeryTemplatingScope
import dev.mokkery.templating.ctx
import dev.mokkery.templating.ext

fun testEvery(block: MokkeryTemplatingScope.() -> Int) {
    every(block = <!FUNCTIONAL_PARAM_MUST_BE_LAMBDA!>block<!>)
    everySuspend(<!FUNCTIONAL_PARAM_MUST_BE_LAMBDA!>block<!>)
}

fun testVerify(block: MokkeryTemplatingScope.() -> Unit) {
    verify(block = <!FUNCTIONAL_PARAM_MUST_BE_LAMBDA!>block<!>)
    verifySuspend(block = <!FUNCTIONAL_PARAM_MUST_BE_LAMBDA!>block<!>)
}

interface ContextFoo {
    context(value: Int)
    fun foo(): Int
}

fun testContextFunctions(mock: ContextFoo, ctxBlock: context(Int) () -> Int, extBlock: ContextFoo.() -> Int) {
    every { ctx(1, <!FUNCTIONAL_PARAM_MUST_BE_LAMBDA!>ctxBlock<!>) }
    every { ctx(block = <!FUNCTIONAL_PARAM_MUST_BE_LAMBDA!>ctxBlock<!>, a = 1) }
    every { mock.ext(<!FUNCTIONAL_PARAM_MUST_BE_LAMBDA!>extBlock<!>) }
    every { ctx(1) { mock.foo() } }
    every { mock.ext { ctx(1) { foo() } } }
}
