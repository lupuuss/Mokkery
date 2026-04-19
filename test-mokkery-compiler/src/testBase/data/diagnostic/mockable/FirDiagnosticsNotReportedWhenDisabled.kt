// DISABLE_FIR_DIAGNOSTICS

import dev.mokkery.mockable.annotations.Mockable

open class NotMockableBase

@Mockable
class Derived : NotMockableBase()
