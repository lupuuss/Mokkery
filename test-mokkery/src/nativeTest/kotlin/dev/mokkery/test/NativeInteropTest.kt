package dev.mokkery.test

import dev.mokkery.answering.returnsArgAt
import dev.mokkery.every
import dev.mokkery.matcher.any
import dev.mokkery.mock
import dev.mokkery.verify
import dev.mokkery.verify.VerifyMode.Companion.not
import kotlin.test.Test
import platform.posix.FILE

class NativeInteropTest {

    @Test
    fun testInteropClassesInteractions() {
        val mock = mock<NativeInteropInterface> { every { call(any()) } returnsArgAt 0 }
        verify(not) { mock.call(any()) }
    }

    @Test
    fun testInteropGenericsClassesInteractions() {
        val mock = mock<NativeInteropInterface> { every { callGeneric(any<FILE>()) } returnsArgAt 0 }
        verify(not) { mock.callGeneric(any<FILE>()) }
    }
}
