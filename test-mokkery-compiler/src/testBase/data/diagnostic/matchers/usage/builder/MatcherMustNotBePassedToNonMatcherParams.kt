import dev.mokkery.matcher.MokkeryMatcherScope
import dev.mokkery.matcher.any
import dev.mokkery.matcher.gte

fun <T : Comparable<T>> MokkeryMatcherScope.matcher(): T = gte(<!MATCHER_PASSED_TO_NON_MATCHER_PARAM!>any()<!>)
