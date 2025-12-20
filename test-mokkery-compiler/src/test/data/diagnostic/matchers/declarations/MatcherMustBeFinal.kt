import dev.mokkery.matcher.MokkeryMatcherScope

interface Foo {

    <!MATCHER_MUST_BE_FINAL!>fun <T> MokkeryMatcherScope.matcher(): T<!>
}
