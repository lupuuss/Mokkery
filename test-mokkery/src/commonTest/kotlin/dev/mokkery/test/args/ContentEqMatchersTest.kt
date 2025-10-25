@file:OptIn(ExperimentalUnsignedTypes::class)

package dev.mokkery.test.args

import dev.mokkery.MokkeryRuntimeException
import dev.mokkery.answering.returns
import dev.mokkery.every
import dev.mokkery.matcher.collections.contentEq
import dev.mokkery.mock
import dev.mokkery.test.CollectionsInterface
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class ContentEqMatchersTest {

    private val mock = mock<CollectionsInterface>()

    @Test
    fun testElementsArray() {
        val arr = arrayOf("1", "2", "3")
        every { mock.callWithElementsArray(contentEq(arr)) } returns "Hello!"
        assertEquals("Hello!", mock.callWithElementsArray(arrayOf("1", "2", "3")))
        assertFailsWith<MokkeryRuntimeException> { mock.callWithElementsArray(arrayOf("3", "2", "1")) }
        assertFailsWith<MokkeryRuntimeException> { mock.callWithElementsArray(arrayOf("1", "2")) }
    }

    @Test
    fun testBooleansArray() {
        val arr = booleanArrayOf(true, false)
        every { mock.callWithBooleansArray(contentEq(arr)) } returns true
        assertEquals(true, mock.callWithBooleansArray(booleanArrayOf(true, false)))
        assertFailsWith<MokkeryRuntimeException> { mock.callWithBooleansArray(booleanArrayOf(false, true)) }
    }

    @Test
    fun testCharsArray() {
        val arr = charArrayOf('a', 'b')
        every { mock.callWithCharsArray(contentEq(arr)) } returns 'x'
        assertEquals('x', mock.callWithCharsArray(charArrayOf('a', 'b')))
        assertFailsWith<MokkeryRuntimeException> { mock.callWithCharsArray(charArrayOf('b', 'a')) }
    }

    @Test
    fun testBytesArray() {
        val arr = byteArrayOf(1, 2, 3)
        every { mock.callWithBytesArray(contentEq(arr)) } returns 42
        assertEquals(42, mock.callWithBytesArray(byteArrayOf(1, 2, 3)))
        assertFailsWith<MokkeryRuntimeException> { mock.callWithBytesArray(byteArrayOf(3, 2, 1)) }
    }

    @Test
    fun testUBytesArray() {
        val arr = ubyteArrayOf(1u, 2u, 3u)
        every { mock.callWithUBytesArray(contentEq(arr)) } returns 42u
        assertEquals(42u, mock.callWithUBytesArray(ubyteArrayOf(1u, 2u, 3u)))
        assertFailsWith<MokkeryRuntimeException> { mock.callWithUBytesArray(ubyteArrayOf(3u, 2u, 1u)) }
    }

    @Test
    fun testShortsArray() {
        val arr = shortArrayOf(1, 2)
        every { mock.callWithShortsArray(contentEq(arr)) } returns 42
        assertEquals(42, mock.callWithShortsArray(shortArrayOf(1, 2)))
        assertFailsWith<MokkeryRuntimeException> { mock.callWithShortsArray(shortArrayOf(2, 1)) }
    }

    @Test
    fun testUShortsArray() {
        val arr = ushortArrayOf(1u, 2u)
        every { mock.callWithUShortsArray(contentEq(arr)) } returns 42u
        assertEquals(42u, mock.callWithUShortsArray(ushortArrayOf(1u, 2u)))
        assertFailsWith<MokkeryRuntimeException> { mock.callWithUShortsArray(ushortArrayOf(2u, 1u)) }
    }

    @Test
    fun testIntsArray() {
        val arr = intArrayOf(1, 2, 3)
        every { mock.callWithIntsArray(contentEq(arr)) } returns 42
        assertEquals(42, mock.callWithIntsArray(intArrayOf(1, 2, 3)))
        assertFailsWith<MokkeryRuntimeException> { mock.callWithIntsArray(intArrayOf(3, 2, 1)) }
    }

    @Test
    fun testUIntsArray() {
        val arr = uintArrayOf(1u, 2u, 3u)
        every { mock.callWithUIntsArray(contentEq(arr)) } returns 42u
        assertEquals(42u, mock.callWithUIntsArray(uintArrayOf(1u, 2u, 3u)))
        assertFailsWith<MokkeryRuntimeException> { mock.callWithUIntsArray(uintArrayOf(3u, 2u, 1u)) }
    }

    @Test
    fun testLongsArray() {
        val arr = longArrayOf(1L, 2L)
        every { mock.callWithLongsArray(contentEq(arr)) } returns 42L
        assertEquals(42L, mock.callWithLongsArray(longArrayOf(1L, 2L)))
        assertFailsWith<MokkeryRuntimeException> { mock.callWithLongsArray(longArrayOf(2L, 1L)) }
    }

    @Test
    fun testULongsArray() {
        val arr = ulongArrayOf(1uL, 2uL)
        every { mock.callWithULongsArray(contentEq(arr)) } returns 42uL
        assertEquals(42uL, mock.callWithULongsArray(ulongArrayOf(1uL, 2uL)))
        assertFailsWith<MokkeryRuntimeException> { mock.callWithULongsArray(ulongArrayOf(2uL, 1uL)) }
    }

    @Test
    fun testFloatsArray() {
        val arr = floatArrayOf(1f, 2f)
        every { mock.callWithFloatsArray(contentEq(arr)) } returns 42f
        assertEquals(42f, mock.callWithFloatsArray(floatArrayOf(1f, 2f)))
        assertFailsWith<MokkeryRuntimeException> { mock.callWithFloatsArray(floatArrayOf(2f, 1f)) }
    }

    @Test
    fun testDoublesArray() {
        val arr = doubleArrayOf(1.0, 2.0)
        every { mock.callWithDoublesArray(contentEq(arr)) } returns 42.0
        assertEquals(42.0, mock.callWithDoublesArray(doubleArrayOf(1.0, 2.0)))
        assertFailsWith<MokkeryRuntimeException> { mock.callWithDoublesArray(doubleArrayOf(2.0, 1.0)) }
    }
}
