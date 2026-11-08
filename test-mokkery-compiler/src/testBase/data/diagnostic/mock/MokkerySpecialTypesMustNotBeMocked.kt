import dev.mokkery.MokkeryCallScope
import dev.mokkery.MokkeryScope
import dev.mokkery.MokkerySuiteScope
import dev.mokkery.matcher.ArgMatcher
import dev.mokkery.matcher.MokkeryMatcherScope
import dev.mokkery.mock
import dev.mokkery.mockMany
import dev.mokkery.spy
import dev.mokkery.templating.MokkeryTemplatingScope

interface CustomMatcher : ArgMatcher<Int>

interface CustomScope : MokkeryScope

interface MatcherMembers {
    val matcher: ArgMatcher<Int>
    fun call(matcher: ArgMatcher<Int>): ArgMatcher<Int>?
    fun call(vararg matchers: ArgMatcher<Int>)
}

interface ScopeMembers {
    fun MokkeryScope.call()
    context(scope: MokkeryCallScope)
    fun call()
    fun call(scope: MokkerySuiteScope)
}

interface Generic<T> {
    fun call(value: T)
}

fun test(matcher: ArgMatcher<Int>, scope: MokkerySuiteScope) {
    mock<<!SPECIAL_TYPE_CANNOT_BE_INTERCEPTED!>MokkeryScope<!>>()
    mock<<!SPECIAL_TYPE_CANNOT_BE_INTERCEPTED!>MokkeryCallScope<!>>()
    mock<<!SPECIAL_TYPE_CANNOT_BE_INTERCEPTED!>MokkeryMatcherScope<!>>()
    mock<<!SPECIAL_TYPE_CANNOT_BE_INTERCEPTED!>MokkeryTemplatingScope<!>>()
    mock<<!SPECIAL_TYPE_CANNOT_BE_INTERCEPTED!>CustomScope<!>>()
    mockMany<Generic<Int>, <!SPECIAL_TYPE_CANNOT_BE_INTERCEPTED!>CustomScope<!>>()
    <!SPECIAL_TYPE_CANNOT_BE_INTERCEPTED!>spy<!>(scope)
    mock<ArgMatcher<Int>>()
    mock<CustomMatcher>()
    spy(matcher)
    mock<MatcherMembers>()
    mock<ScopeMembers>()
    mock<Generic<ArgMatcher<Int>>>()
    mock<Generic<in ArgMatcher<Int>>>()
}
