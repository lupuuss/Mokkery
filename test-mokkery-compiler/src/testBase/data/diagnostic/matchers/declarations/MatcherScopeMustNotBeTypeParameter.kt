import dev.mokkery.matcher.MokkeryMatcherScope
import dev.mokkery.matcher.gt
import dev.mokkery.templating.MokkeryTemplatingScope

fun <S : MokkeryMatcherScope> <!MATCHER_SCOPE_MUST_NOT_BE_TYPE_PARAMETER!>S<!>.genericReceiver(): Int = gt(1)

fun <S : MokkeryMatcherScope> genericParameter(<!MATCHER_SCOPE_MUST_NOT_BE_TYPE_PARAMETER!>scope: S<!>): Int = 1

context(<!MATCHER_SCOPE_MUST_NOT_BE_TYPE_PARAMETER!>scope: S<!>)
fun <S : MokkeryMatcherScope> genericContext(): Int = 1

val <S : MokkeryMatcherScope> <!MATCHER_SCOPE_MUST_NOT_BE_TYPE_PARAMETER!>S<!>.genericProperty: Int
    get() = 1

fun <S : MokkeryMatcherScope?> <!MATCHER_SCOPE_MUST_NOT_BE_TYPE_PARAMETER!>S<!>.nullableBound(): Int = 1

fun <S : MokkeryTemplatingScope> <!MATCHER_SCOPE_MUST_NOT_BE_TYPE_PARAMETER!>S<!>.subtypeBound(): Int = 1

fun <T> T.unbounded(): Int = 1

fun <T> MokkeryMatcherScope.genericMatcher(value: T): Int = gt(1)

data class GenericScopeHolder<S : MokkeryMatcherScope>(<!MATCHER_SCOPE_MUST_NOT_BE_TYPE_PARAMETER!>val scope: S<!>)
