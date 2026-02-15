import dev.mokkery.matcher.MokkeryMatcherScope
import dev.mokkery.matcher.any

fun MokkeryMatcherScope.matcher(): Int {
    val matcher = any<Int>()
    when (<!ILLEGAL_MATCHER_IN_WHEN_SUBJECT!>matcher<!>) {
        else -> Unit
    }
    when (val m = <!ILLEGAL_MATCHER_IN_WHEN_SUBJECT!>matcher<!>) {
        else -> Unit
    }
    when (<!ILLEGAL_MATCHER_IN_WHEN_SUBJECT!>any<Int>()<!>) {
        else -> Unit
    }
    when (val m = <!ILLEGAL_MATCHER_IN_WHEN_SUBJECT!>any<Int>()<!>) {
        else -> Unit
    }
    return matcher
}
