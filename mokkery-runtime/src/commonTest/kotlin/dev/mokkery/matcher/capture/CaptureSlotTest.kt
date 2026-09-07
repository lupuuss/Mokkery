package dev.mokkery.matcher.capture

import dev.mokkery.internal.AbsentValueInSlotException
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class CaptureSlotTest {
    private val slot = Capture.slot<Int>()

    @Test
    fun testKeepsOnlyLastValue() {
        slot.capture(1)
        slot.capture(2)
        slot.capture(3)
        assertEquals(listOf(3), slot.values)
    }

    @Test
    fun testValueReturnsAbsentWhenNoValue() {
        assertEquals(SlotCapture.Value.Absent, slot.value)
    }

    @Test
    fun testValueReturnsPresentWhenValueCaptured() {
        slot.capture(1)
        assertEquals(SlotCapture.Value.Present(1), slot.value)
    }

    @Test
    fun testIsPresentReturnsTrueOnPresentValue() {
        slot.capture(1)
        assertTrue(slot.isPresent)
    }

    @Test
    fun testIsPresentReturnsFalseOnAbsentValue() {
        assertFalse(slot.isPresent)
    }

    @Test
    fun testIsAbsentReturnsFalseOnPresentValue() {
        slot.capture(1)
        assertFalse(slot.isAbsent)
    }

    @Test
    fun testIsAbsentReturnsTrueOnAbsentValue() {
        assertTrue(slot.isAbsent)
    }

    @Test
    fun testGetIfPresentReturnsNullWhenValueAbsent() {
        assertEquals(null, slot.getIfPresent())
    }

    @Test
    fun testGetIfPresentReturnsValueWhenValuePresent() {
        slot.capture(1)
        assertEquals(1, slot.getIfPresent())
    }

    @Test
    fun testGetFailsWhenValueAbsent() {
        assertFailsWith<AbsentValueInSlotException> {
            slot.get()
        }
    }

    @Test
    fun testGetReturnsValueWhenValuePresent() {
        slot.capture(1)
        assertEquals(1, slot.get())
    }

    @Test
    fun testValueReturnsPresentWhenNullCaptured() {
        val nullableSlot = Capture.slot<Int?>()
        nullableSlot.capture(null)
        assertEquals(SlotCapture.Value.Present(null), nullableSlot.value)
        assertTrue(nullableSlot.isPresent)
        assertFalse(nullableSlot.isAbsent)
    }

    @Test
    fun testGetReturnsNullWhenNullCaptured() {
        val nullableSlot = Capture.slot<Int?>()
        nullableSlot.capture(null)
        assertEquals(null, nullableSlot.get())
    }

    @Test
    fun testValueStaysPresentWhenNullCapturedOverValue() {
        val nullableSlot = Capture.slot<Int?>()
        nullableSlot.capture(1)
        nullableSlot.capture(null)
        assertEquals(listOf(null), nullableSlot.values)
        assertTrue(nullableSlot.isPresent)
    }
}
