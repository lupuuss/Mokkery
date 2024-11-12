package dev.mokkery.test

import dev.mokkery.MockMode.autoUnit
import dev.mokkery.MockMode.autofill
import dev.mokkery.MockMode.original
import dev.mokkery.MockMode.strict
import dev.mokkery.MokkeryRuntimeException
import dev.mokkery.mock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class MockModesTest {

    @Test
    fun testStrictModeAlwaysFailOnUnansweredCall() {
        val strictMock = mock<RegularMethodsInterface>(strict)
        assertFailsWith<MokkeryRuntimeException> { strictMock.callUnit(Unit) }
        assertFailsWith<MokkeryRuntimeException> { strictMock.callPrimitive(1) }
        assertFailsWith<MokkeryRuntimeException> { strictMock.callComplex(ComplexType) }
    }

    @Test
    fun testAutoUnitModeSucceedsOnRegularUnitReturningCalls() {
        val autoUnitMock = mock<RegularMethodsInterface>(autoUnit)
        autoUnitMock.callUnit(Unit)
    }

    @Test
    fun testAutoUnitModeSucceedsOnClassGenericUnitReturningCalls() {
        val autoUnitMock = mock<GenericFunctionsInterface<Unit>>(autoUnit)
        autoUnitMock.call(Unit)
    }

    @Test
    fun testAutoUnitModeFailsOnNonUnitReturningCalls() {
        val autoUnitMock = mock<RegularMethodsInterface>(autoUnit)
        assertFailsWith<MokkeryRuntimeException> { autoUnitMock.callPrimitive(1) }
        assertFailsWith<MokkeryRuntimeException> { autoUnitMock.callComplex(ComplexType) }
    }

    @Test
    fun testAutoUnitModeFailsOnClassGenericNonUnitReturningCalls() {
        val autoUnitMock = mock<GenericFunctionsInterface<Unit>>(autoUnit)
        assertFailsWith<MokkeryRuntimeException> { autoUnitMock.callGenericWithStarProjection(listOf<Int>()) }
    }

    @Test
    fun testAutofillModeSucceedsOnAnyCall() {
        val autofillMock = mock<RegularMethodsInterface>(autofill)
        assertEquals(0, autofillMock.callPrimitive(1))
        assertEquals(Unit, autofillMock.callUnit(Unit))
        assertEquals("", autofillMock.callOverloaded(""))
    }

    @Test
    fun testOriginalModeCallsOriginalMethodsWhenAvailable() {
        val originalMock = mock<DefaultsInterfaceLevel1<Int>>(original)
        assertEquals(ComplexType("1"), originalMock.call(1, ComplexType))
        assertEquals(ComplexType("2"), originalMock.callIndirectDefault(1, ComplexType))
        assertEquals(null, originalMock.property)
    }

    @Test
    fun testOriginalModeFailsWhenOriginalMethodNotAvailable() {
        val originalMock = mock<DefaultsInterfaceLevel1<Int>>(original)
        assertFailsWith<MokkeryRuntimeException> { originalMock.callNoDefault() }
    }
}
