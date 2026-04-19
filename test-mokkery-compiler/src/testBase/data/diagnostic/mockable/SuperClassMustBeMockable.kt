import dev.mokkery.mockable.annotations.Mockable

open class NotMockableBase

<!SUPER_CLASS_MUST_BE_MOCKABLE!>@Mockable
class Derived : NotMockableBase()<!>
