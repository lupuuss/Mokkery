package dev.mokkery.test

import dev.mokkery.MokkeryScope
import dev.mokkery.annotations.InternalMokkeryApi
import dev.mokkery.answering.returns
import dev.mokkery.every
import dev.mokkery.matcher.any
import dev.mokkery.mock

@OptIn(InternalMokkeryApi::class)
val mainMokkeryModule = MokkeryScope.module

fun mockCreatedInMainModule(value: Int): RegularMethodsInterface = mock {
    every { callPrimitive(any()) } returns value
}
