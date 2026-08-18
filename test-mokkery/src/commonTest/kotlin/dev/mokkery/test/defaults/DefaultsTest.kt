package dev.mokkery.test.defaults

import dev.mokkery.MokkeryRuntimeException
import dev.mokkery.answering.returnsArgAt
import dev.mokkery.every
import dev.mokkery.everySuspend
import dev.mokkery.mock
import dev.mokkery.templating.ext
import dev.mokkery.test.FunctionDefaultsInterface
import dev.mokkery.test.GenericDefaultsInterface
import dev.mokkery.verify
import dev.mokkery.verifySuspend
import kotlinx.coroutines.test.runTest
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

    @Test
    fun testWithComputedDefaultsForSuspend() = runTest {
        everySuspend { mock.callSuspend(5) } returnsArgAt 2
        assertEquals("name5@mail.com", mock.callSuspend(5))
        assertEquals("name5@mail.com", mock.callSuspend(5, "name(5)"))
        assertFailsWith<MokkeryRuntimeException> { mock.callSuspend(5, "not-name") }
        verifySuspend { mock.callSuspend(5) }
    }

    @Test
    fun testWithComputedDefaultsForExtension() {
        every { mock.ext { 5.callExtension() } } returnsArgAt 2
        assertEquals("name5@mail.com", mock.run { 5.callExtension() })
        assertEquals("name5@mail.com", mock.run { 5.callExtension("name(5)") })
        assertFailsWith<MokkeryRuntimeException> { mock.run { 5.callExtension("not-name") } }
        assertFailsWith<MokkeryRuntimeException> { mock.run { 4.callExtension() } }
        verify { mock.ext { 5.callExtension() } }
    }

    @Test
    fun testWithComputedDefaultsForGenericType() {
        val generic = mock<GenericDefaultsInterface<Int>>()
        every { generic.call(5) } returnsArgAt 2
        assertEquals("name(5)", generic.run { call(5) })
        assertFailsWith<MokkeryRuntimeException> { generic.call(5, 1) }
        verify { generic.call(5) }
    }

    @Test
    fun testDoesNotAffectIdsOfMocksCreatedAfterExtraction() {
        val first = mock<FunctionDefaultsInterface>()
        every { first.call(5) } returnsArgAt 1
        first.call(5)
        val second = mock<FunctionDefaultsInterface>()
        assertEquals(first.instanceIdNumber() + 1, second.instanceIdNumber())
    }

    private fun Any.instanceIdNumber(): Long = toString()
        .substringAfterLast("(")
        .removeSuffix(")")
        .toLong()
}
