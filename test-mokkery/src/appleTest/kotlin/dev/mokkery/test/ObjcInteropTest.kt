package dev.mokkery.test

import dev.mokkery.answering.returnsArgAt
import dev.mokkery.every
import dev.mokkery.matcher.any
import dev.mokkery.mock
import dev.mokkery.verify
import dev.mokkery.verify.VerifyMode.Companion.not
import kotlin.test.Test
import platform.Foundation.NSDecimalNumber
import kotlin.test.Ignore

// https://github.com/lupuuss/Mokkery/issues/63
@Ignore
class ObjcInteropTest {

    @Test
    fun testInteropClassesInteractions() {
        val mock = mock<ObjcInteropInterface> { every { call(any()) } returnsArgAt 0 }
        verify(not) { mock.call(any()) }
    }

    @Test
    fun testInteropGenericsClassesInteractions() {
        val mock = mock<ObjcInteropInterface> { every { callGeneric(any<NSDecimalNumber>()) } returnsArgAt 0 }
        verify(not) { mock.callGeneric(any<NSDecimalNumber>()) }
    }
}
