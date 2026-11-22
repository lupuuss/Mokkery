import dev.mokkery.matcher.MokkeryMatcherScope
import dev.mokkery.matcher.gt

fun interface Mapper<T, R> {
    fun map(value: T): R
}

interface ValueProvider<T> {
    val T.value: Int
}

class MapperImpl : Mapper<MokkeryMatcherScope, Int>, ValueProvider<MokkeryMatcherScope> {

    <!MATCHER_MUST_NOT_OVERRIDE!>final override fun map(value: MokkeryMatcherScope): Int = value.gt(1)<!>

    override val MokkeryMatcherScope.value: Int
        <!MATCHER_MUST_NOT_OVERRIDE!>get() = gt(1)<!>
}
