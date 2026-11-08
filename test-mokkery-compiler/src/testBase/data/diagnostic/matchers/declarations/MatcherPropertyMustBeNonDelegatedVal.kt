import dev.mokkery.matcher.MokkeryMatcherScope
import dev.mokkery.matcher.gt

val MokkeryMatcherScope.extensionProperty: Int
    get() = gt(1)

context(scope: MokkeryMatcherScope)
val contextProperty: Int
    get() = scope.gt(1)

var MokkeryMatcherScope.mutableProperty: Int
    <!MATCHER_MUST_BE_REGULAR_FUNCTION!>get() = gt(1)<!>
    <!MATCHER_MUST_BE_REGULAR_FUNCTION!>set(value) {}<!>

<!MATCHER_MUST_BE_REGULAR_FUNCTION!>val MokkeryMatcherScope.delegatedProperty: Int by lazy { 1 }<!>

open class Holder {
    open val MokkeryMatcherScope.openProperty: Int
        <!MATCHER_MUST_BE_FINAL!>get() = gt(1)<!>
}
