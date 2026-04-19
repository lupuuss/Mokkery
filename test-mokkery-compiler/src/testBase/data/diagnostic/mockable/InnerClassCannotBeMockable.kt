import dev.mokkery.mockable.annotations.Mockable

open class Outer {
    <!INNER_CLASS_CANNOT_BE_MOCKABLE!>@Mockable
    inner class Inner<!>
}
