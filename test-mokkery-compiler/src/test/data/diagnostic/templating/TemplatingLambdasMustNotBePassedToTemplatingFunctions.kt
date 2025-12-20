import dev.mokkery.every
import dev.mokkery.everySuspend
import dev.mokkery.verify
import dev.mokkery.verifySuspend
import dev.mokkery.templating.MokkeryTemplatingScope

fun testEvery(block: MokkeryTemplatingScope.() -> Int) {
    every(block = <!FUNCTIONAL_PARAM_MUST_BE_LAMBDA!>block<!>)
    everySuspend(<!FUNCTIONAL_PARAM_MUST_BE_LAMBDA!>block<!>)
}

fun testVerify(block: MokkeryTemplatingScope.() -> Unit) {
    verify(block = <!FUNCTIONAL_PARAM_MUST_BE_LAMBDA!>block<!>)
    verifySuspend(block = <!FUNCTIONAL_PARAM_MUST_BE_LAMBDA!>block<!>)
}
