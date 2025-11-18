@file:OptIn(ExperimentalUnsignedTypes::class)

package dev.mokkery.test.args

import dev.mokkery.MokkeryRuntimeException
import dev.mokkery.answering.returns
import dev.mokkery.every
import dev.mokkery.matcher.collections.containsAny
import dev.mokkery.matcher.collections.containsAnyBoolean
import dev.mokkery.matcher.collections.containsAnyByte
import dev.mokkery.matcher.collections.containsAnyChar
import dev.mokkery.matcher.collections.containsAnyDouble
import dev.mokkery.matcher.collections.containsAnyElement
import dev.mokkery.matcher.collections.containsAnyFloat
import dev.mokkery.matcher.collections.containsAnyInt
import dev.mokkery.matcher.collections.containsAnyLong
import dev.mokkery.matcher.collections.containsAnyShort
import dev.mokkery.matcher.collections.containsAnyUByte
import dev.mokkery.matcher.collections.containsAnyUInt
import dev.mokkery.matcher.collections.containsAnyULong
import dev.mokkery.matcher.collections.containsAnyUShort
import dev.mokkery.mock
import dev.mokkery.test.CollectionsInterface
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
class ContainsAnyMatchersArrayTest {

    private val mock = mock<CollectionsInterface>()

    @Test
    fun testList() {
        every { mock.callWithList<String>(containsAny { it == "2" }) } returns "Hello!"
        assertEquals("Hello!", mock.callWithList(listOf("0", "1", "2", "10")))
        assertEquals("Hello!", mock.callWithList(listOf("2", "10")))
        assertEquals("Hello!", mock.callWithList(listOf("1", "2")))
        assertFailsWith<MokkeryRuntimeException> { mock.callWithList(listOf("1", "10")) }
        assertFailsWith<MokkeryRuntimeException> { mock.callWithList(listOf()) }
    }

    @Test
    fun testElementsArray() {
        every { mock.callWithElementsArray<String>(containsAnyElement { it == "2" }) } returns "Hello!"
        assertEquals("Hello!", mock.callWithElementsArray(arrayOf("0", "1", "2", "10")))
        assertEquals("Hello!", mock.callWithElementsArray(arrayOf("0", "2", "10")))
        assertEquals("Hello!", mock.callWithElementsArray(arrayOf("0", "2", "3", "10")))
        assertFailsWith<MokkeryRuntimeException> { mock.callWithElementsArray(arrayOf("0", "10")) }
        assertFailsWith<MokkeryRuntimeException> { mock.callWithElementsArray(arrayOf("0", "5", "10")) }
    }

    @Test
    fun testBooleansArray() {
        every { mock.callWithBooleansArray(containsAnyBoolean { it }) } returns true
        assertEquals(true, mock.callWithBooleansArray(booleanArrayOf(true, false, true)))
        assertEquals(true, mock.callWithBooleansArray(booleanArrayOf(true, true, false)))
        assertFailsWith<MokkeryRuntimeException> { mock.callWithBooleansArray(booleanArrayOf(false, false)) }
    }

    @Test
    fun testCharsArray() {
        every { mock.callWithCharsArray(containsAnyChar { it == 'b' }) } returns 'x'
        assertEquals('x', mock.callWithCharsArray(charArrayOf('a', 'b', 'z')))
        assertEquals('x', mock.callWithCharsArray(charArrayOf('a', 'b', 'c', 'z')))
        assertFailsWith<MokkeryRuntimeException> { mock.callWithCharsArray(charArrayOf('a', 'z')) }
    }

    @Test
    fun testBytesArray() {
        every { mock.callWithBytesArray(containsAnyByte { it == 2.toByte() }) } returns 42.toByte()
        assertEquals(42.toByte(), mock.callWithBytesArray(byteArrayOf(0, 1, 2, 10)))
        assertEquals(42.toByte(), mock.callWithBytesArray(byteArrayOf(0, 2, 10)))
        assertFailsWith<MokkeryRuntimeException> { mock.callWithBytesArray(byteArrayOf(0, 10)) }
    }

    @Test
    fun testUBytesArray() {
        every { mock.callWithUBytesArray(containsAnyUByte { it == 2.toUByte() }) } returns 42u
        assertEquals(42u, mock.callWithUBytesArray(ubyteArrayOf(0u, 1u, 2u, 10u)))
        assertEquals(42u, mock.callWithUBytesArray(ubyteArrayOf(0u, 2u, 10u)))
        assertFailsWith<MokkeryRuntimeException> { mock.callWithUBytesArray(ubyteArrayOf(0u, 10u)) }
    }

    @Test
    fun testShortsArray() {
        every { mock.callWithShortsArray(containsAnyShort { it == 2.toShort() }) } returns 42.toShort()
        assertEquals(42.toShort(), mock.callWithShortsArray(shortArrayOf(0, 1, 2, 10)))
        assertEquals(42.toShort(), mock.callWithShortsArray(shortArrayOf(0, 2, 10)))
        assertFailsWith<MokkeryRuntimeException> { mock.callWithShortsArray(shortArrayOf(0, 10)) }
    }

    @Test
    fun testUShortsArray() {
        every { mock.callWithUShortsArray(containsAnyUShort { it == 2.toUShort() }) } returns 42u
        assertEquals(42u, mock.callWithUShortsArray(ushortArrayOf(0u, 1u, 2u, 10u)))
        assertEquals(42u, mock.callWithUShortsArray(ushortArrayOf(0u, 2u, 10u)))
        assertFailsWith<MokkeryRuntimeException> { mock.callWithUShortsArray(ushortArrayOf(0u, 10u)) }
    }

    @Test
    fun testIntsArray() {
        every { mock.callWithIntsArray(containsAnyInt { it == 2 }) } returns 42
        assertEquals(42, mock.callWithIntsArray(intArrayOf(0, 1, 2, 10)))
        assertEquals(42, mock.callWithIntsArray(intArrayOf(0, 2, 10)))
        assertFailsWith<MokkeryRuntimeException> { mock.callWithIntsArray(intArrayOf(0, 10)) }
    }

    @Test
    fun testUIntsArray() {
        every { mock.callWithUIntsArray(containsAnyUInt { it == 2u }) } returns 42u
        assertEquals(42u, mock.callWithUIntsArray(uintArrayOf(0u, 1u, 2u, 10u)))
        assertEquals(42u, mock.callWithUIntsArray(uintArrayOf(0u, 2u, 10u)))
        assertFailsWith<MokkeryRuntimeException> { mock.callWithUIntsArray(uintArrayOf(0u, 10u)) }
    }

    @Test
    fun testLongsArray() {
        every { mock.callWithLongsArray(containsAnyLong { it == 2L }) } returns 42L
        assertEquals(42L, mock.callWithLongsArray(longArrayOf(0L, 1L, 2L, 10L)))
        assertEquals(42L, mock.callWithLongsArray(longArrayOf(0L, 2L, 10L)))
        assertFailsWith<MokkeryRuntimeException> { mock.callWithLongsArray(longArrayOf(0L, 10L)) }
    }

    @Test
    fun testULongsArray() {
        every { mock.callWithULongsArray(containsAnyULong { it == 2uL }) } returns 42uL
        assertEquals(42uL, mock.callWithULongsArray(ulongArrayOf(0uL, 1uL, 2uL, 10uL)))
        assertEquals(42uL, mock.callWithULongsArray(ulongArrayOf(0uL, 2uL, 10uL)))
        assertFailsWith<MokkeryRuntimeException> { mock.callWithULongsArray(ulongArrayOf(0uL, 10uL)) }
    }

    @Test
    fun testFloatsArray() {
        every { mock.callWithFloatsArray(containsAnyFloat { it == 2f }) } returns 42f
        assertEquals(42f, mock.callWithFloatsArray(floatArrayOf(0f, 1f, 2f, 10f)))
        assertEquals(42f, mock.callWithFloatsArray(floatArrayOf(0f, 2f, 10f)))
        assertFailsWith<MokkeryRuntimeException> { mock.callWithFloatsArray(floatArrayOf(0f, 10f)) }
    }

    @Test
    fun testDoublesArray() {
        every { mock.callWithDoublesArray(containsAnyDouble { it == 2.0 }) } returns 42.0
        assertEquals(42.0, mock.callWithDoublesArray(doubleArrayOf(0.0, 1.0, 2.0, 10.0)))
        assertEquals(42.0, mock.callWithDoublesArray(doubleArrayOf(0.0, 2.0, 10.0)))
        assertFailsWith<MokkeryRuntimeException> { mock.callWithDoublesArray(doubleArrayOf(0.0, 10.0)) }
    }
}
