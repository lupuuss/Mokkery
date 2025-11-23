@file:OptIn(ExperimentalUnsignedTypes::class)

package dev.mokkery.test.args

import dev.mokkery.MokkeryRuntimeException
import dev.mokkery.answering.returns
import dev.mokkery.every
import dev.mokkery.matcher.collections.containsAll
import dev.mokkery.matcher.collections.containsAllBooleans
import dev.mokkery.matcher.collections.containsAllBytes
import dev.mokkery.matcher.collections.containsAllChars
import dev.mokkery.matcher.collections.containsAllDoubles
import dev.mokkery.matcher.collections.containsAllElements
import dev.mokkery.matcher.collections.containsAllFloats
import dev.mokkery.matcher.collections.containsAllInts
import dev.mokkery.matcher.collections.containsAllLongs
import dev.mokkery.matcher.collections.containsAllShorts
import dev.mokkery.matcher.collections.containsAllUBytes
import dev.mokkery.matcher.collections.containsAllUInts
import dev.mokkery.matcher.collections.containsAllULongs
import dev.mokkery.matcher.collections.containsAllUShorts
import dev.mokkery.mock
import dev.mokkery.test.CollectionsInterface
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class ContainsAllMatchersTest {

    private val mock = mock<CollectionsInterface>()


    @Test
    fun testList() {
        every { mock.callWithList<String>(containsAll { it == "2" }) } returns "Hello!"
        assertEquals("Hello!", mock.callWithList(listOf("2", "2")))
        assertEquals("Hello!", mock.callWithList(listOf("2", "2", "2")))
        assertEquals("Hello!", mock.callWithList(listOf()))
        assertFailsWith<MokkeryRuntimeException> { mock.callWithList(listOf("2", "3")) }
    }

    @Test
    fun testElementsArray() {
        every { mock.callWithElementsArray<String>(containsAllElements { it == "2" }) } returns "Hello!"
        assertEquals("Hello!", mock.callWithElementsArray(arrayOf("2", "2")))
        assertEquals("Hello!", mock.callWithElementsArray(arrayOf("2", "2", "2")))
        assertEquals("Hello!", mock.callWithElementsArray(arrayOf()))
        assertFailsWith<MokkeryRuntimeException> { mock.callWithElementsArray(arrayOf("2", "3")) }
    }

    @Test
    fun testBooleansArray() {
        every { mock.callWithBooleansArray(containsAllBooleans { it }) } returns true
        assertEquals(true, mock.callWithBooleansArray(booleanArrayOf(true, true)))
        assertEquals(true, mock.callWithBooleansArray(booleanArrayOf(true, true, true)))
        assertFailsWith<MokkeryRuntimeException> { mock.callWithBooleansArray(booleanArrayOf(true, false)) }
        assertFailsWith<MokkeryRuntimeException> { mock.callWithBooleansArray(booleanArrayOf(false, false)) }
    }

    @Test
    fun testCharsArray() {
        every { mock.callWithCharsArray(containsAllChars { it == 'b' }) } returns 'x'
        assertEquals('x', mock.callWithCharsArray(charArrayOf('b', 'b')))
        assertEquals('x', mock.callWithCharsArray(charArrayOf()))
        assertFailsWith<MokkeryRuntimeException> { mock.callWithCharsArray(charArrayOf('b', 'c')) }
    }

    @Test
    fun testBytesArray() {
        every { mock.callWithBytesArray(containsAllBytes { it == 2.toByte() }) } returns 42.toByte()
        assertEquals(42.toByte(), mock.callWithBytesArray(byteArrayOf(2, 2)))
        assertEquals(42.toByte(), mock.callWithBytesArray(byteArrayOf()))
        assertFailsWith<MokkeryRuntimeException> { mock.callWithBytesArray(byteArrayOf(2, 3)) }
    }

    @Test
    fun testUBytesArray() {
        every { mock.callWithUBytesArray(containsAllUBytes { it == 2.toUByte() }) } returns 42u
        assertEquals(42u, mock.callWithUBytesArray(ubyteArrayOf(2u, 2u)))
        assertEquals(42u, mock.callWithUBytesArray(ubyteArrayOf()))
        assertFailsWith<MokkeryRuntimeException> { mock.callWithUBytesArray(ubyteArrayOf(2u, 3u)) }
    }

    @Test
    fun testShortsArray() {
        every { mock.callWithShortsArray(containsAllShorts { it == 2.toShort() }) } returns 42.toShort()
        assertEquals(42.toShort(), mock.callWithShortsArray(shortArrayOf(2, 2)))
        assertEquals(42.toShort(), mock.callWithShortsArray(shortArrayOf()))
        assertFailsWith<MokkeryRuntimeException> { mock.callWithShortsArray(shortArrayOf(2, 3)) }
    }

    @Test
    fun testUShortsArray() {
        every { mock.callWithUShortsArray(containsAllUShorts { it == 2.toUShort() }) } returns 42u
        assertEquals(42u, mock.callWithUShortsArray(ushortArrayOf(2u, 2u)))
        assertEquals(42u, mock.callWithUShortsArray(ushortArrayOf()))
    }

    @Test
    fun testIntsArray() {
        every { mock.callWithIntsArray(containsAllInts { it == 2 }) } returns 42
        assertEquals(42, mock.callWithIntsArray(intArrayOf(2, 2)))
        assertEquals(42, mock.callWithIntsArray(intArrayOf()))
        assertFailsWith<MokkeryRuntimeException> { mock.callWithIntsArray(intArrayOf(2, 3)) }
    }

    @Test
    fun testUIntsArray() {
        every { mock.callWithUIntsArray(containsAllUInts { it == 2u }) } returns 42u
        assertEquals(42u, mock.callWithUIntsArray(uintArrayOf(2u, 2u)))
        assertEquals(42u, mock.callWithUIntsArray(uintArrayOf()))
        assertFailsWith<MokkeryRuntimeException> { mock.callWithUIntsArray(uintArrayOf(2u, 3u)) }
    }

    @Test
    fun testLongsArray() {
        every { mock.callWithLongsArray(containsAllLongs { it == 2L }) } returns 42L
        assertEquals(42L, mock.callWithLongsArray(longArrayOf(2L, 2L)))
        assertEquals(42L, mock.callWithLongsArray(longArrayOf()))
        assertFailsWith<MokkeryRuntimeException> { mock.callWithLongsArray(longArrayOf(2L, 3L)) }
    }

    @Test
    fun testULongsArray() {
        every { mock.callWithULongsArray(containsAllULongs { it == 2uL }) } returns 42uL
        assertEquals(42uL, mock.callWithULongsArray(ulongArrayOf(2uL, 2uL)))
        assertEquals(42uL, mock.callWithULongsArray(ulongArrayOf()))
        assertFailsWith<MokkeryRuntimeException> { mock.callWithULongsArray(ulongArrayOf(2uL, 3uL)) }
    }

    @Test
    fun testFloatsArray() {
        every { mock.callWithFloatsArray(containsAllFloats { it == 2f }) } returns 42f
        assertEquals(42f, mock.callWithFloatsArray(floatArrayOf(2f, 2f)))
        assertEquals(42f, mock.callWithFloatsArray(floatArrayOf()))
        assertFailsWith<MokkeryRuntimeException> { mock.callWithFloatsArray(floatArrayOf(2f, 3f)) }
    }

    @Test
    fun testDoublesArray() {
        every { mock.callWithDoublesArray(containsAllDoubles { it == 2.0 }) } returns 42.0
        assertEquals(42.0, mock.callWithDoublesArray(doubleArrayOf(2.0, 2.0)))
        assertEquals(42.0, mock.callWithDoublesArray(doubleArrayOf()))
        assertFailsWith<MokkeryRuntimeException> { mock.callWithDoublesArray(doubleArrayOf(2.0, 3.0)) }
    }
}
