package dev.mokkery.test.defaults

import dev.mokkery.MokkeryRuntimeException
import dev.mokkery.answering.returnsArgAt
import dev.mokkery.every
import dev.mokkery.mock
import dev.mokkery.test.FunctionDefaultsInterface
import dev.mokkery.test.assertVerified
import dev.mokkery.verify
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class DefaultsTest {

    private val mock = mock<FunctionDefaultsInterface>()


    @Test
    fun testWithComputedDefaults() {
        every { mock.call(5) } returnsArgAt 1
        assertEquals("name(5)", mock.call(5))
        assertEquals("name(5)", mock.call(5, "name(5)"))
        assertEquals("name(5)", mock.call(5, "name(5)", "name5@mail.com"))
        assertFailsWith<MokkeryRuntimeException> { mock.call(4)  }
        assertFailsWith<MokkeryRuntimeException> { mock.call(5, "not-name") }
        assertFailsWith<MokkeryRuntimeException> { mock.call(5, mail = "not-mail") }
        verify { mock.call(5) }
    }

    @Test
    fun testWithAllValues() {
        every { mock.call(5, "name", "mail") } returnsArgAt 1
        assertEquals("name", mock.call(5, "name", "mail"))
        assertFailsWith<MokkeryRuntimeException> { mock.call(4, "name", "mail") }
        verify { mock.call(5, "name", "mail") }
    }
}
