import dev.mokkery.mockable.annotations.Mockable

fun test() {
    <!LOCAL_CLASS_CANNOT_BE_MOCKABLE!>@Mockable
    class Local<!>
}
