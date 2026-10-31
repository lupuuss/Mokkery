package dev.mokkery.test.types

import dev.mokkery.MockMode
import dev.mokkery.answering.MokkeryProperty.Companion.bound
import dev.mokkery.answering.MokkeryProperty.Companion.field
import dev.mokkery.answering.by
import dev.mokkery.answering.returns
import dev.mokkery.answering.returnsBy
import dev.mokkery.answering.sequentiallyReturns
import dev.mokkery.answering.throws
import dev.mokkery.answering.throwsBy
import dev.mokkery.answering.throwsErrorWith
import dev.mokkery.every
import dev.mokkery.matcher.capture.Capture
import dev.mokkery.matcher.capture.get
import dev.mokkery.mock
import dev.mokkery.test.ComplexType
import dev.mokkery.test.GenericPropertiesInterface
import dev.mokkery.test.PropertiesInterface
import kotlin.properties.Delegates
import kotlin.properties.ReadOnlyProperty
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertSame

class MocksPropertyReferenceTest {

    private val mock = mock<PropertiesInterface>(MockMode.strict)
    private val readOnlyMock = mock<ReadOnlyPropertiesInterface>(MockMode.strict)

    @Test
    fun testGetterByReference() {
        every(mock::primitiveProperty).getter returns 1
        assertEquals(1, mock.primitiveProperty)
    }

    @Test
    fun testComplexGetterByReference() {
        every(mock::complexProperty).getter returns ComplexType.Companion
        assertEquals(ComplexType.Companion, mock.complexProperty)
    }

    @Test
    fun testGenericGetterByReference() {
        val genericMock = mock<GenericPropertiesInterface<String>>(MockMode.strict)
        every(genericMock::property).getter returns "foo"
        assertEquals("foo", genericMock.property)
    }

    @Test
    fun testSetterHasNoAnswerUntilConfigured() {
        every(mock::primitiveProperty) { getter returns 1 }
        assertFailsWith<Throwable> { mock.primitiveProperty = 99 }
    }

    @Test
    fun testBlockConfiguresGetterAndSetterIndependently() {
        every(mock::primitiveProperty) {
            getter returns 1
            setter throws IllegalStateException()
        }
        assertEquals(1, mock.primitiveProperty)
        assertFailsWith<IllegalStateException> { mock.primitiveProperty = 123 }
    }

    @Test
    fun testMutableReturnsIgnoresWrites() {
        every(mock::primitiveProperty) returns 1
        mock.primitiveProperty = 2
        assertEquals(1, mock.primitiveProperty)
    }

    @Test
    fun testMutableReturnsByIgnoresWrites() {
        var counter = 0
        every(mock::primitiveProperty) returnsBy { ++counter }
        mock.primitiveProperty = 10
        assertEquals(1, mock.primitiveProperty)
        assertEquals(2, mock.primitiveProperty)
    }

    @Test
    fun testMutableThrowsOnBothAccessors() {
        every(mock::primitiveProperty) throws IllegalArgumentException()
        assertFailsWith<IllegalArgumentException> { mock.primitiveProperty }
        assertFailsWith<IllegalArgumentException> { mock.primitiveProperty = 1 }
    }

    @Test
    fun testMutableThrowsByOnBothAccessors() {
        every(mock::primitiveProperty) throwsBy { IllegalArgumentException() }
        assertFailsWith<IllegalArgumentException> { mock.primitiveProperty }
        assertFailsWith<IllegalArgumentException> { mock.primitiveProperty = 1 }
    }

    @Test
    fun testMutableThrowsErrorWithOnBothAccessors() {
        every(mock::primitiveProperty) throwsErrorWith "failed"
        assertEquals("failed", assertFailsWith<IllegalStateException> { mock.primitiveProperty }.message)
        assertEquals("failed", assertFailsWith<IllegalStateException> { mock.primitiveProperty = 1 }.message)
    }

    @Test
    fun testMutableTopLevelAnswerCanBeOverriddenPerAccessor() {
        every(mock::primitiveProperty) returns 1
        every(mock::primitiveProperty).setter throws IllegalStateException()
        assertEquals(1, mock.primitiveProperty)
        assertFailsWith<IllegalStateException> { mock.primitiveProperty = 2 }
    }

    @Test
    fun testField() {
        every(mock::primitiveProperty) by field(1)
        assertEquals(1, mock.primitiveProperty)
        mock.primitiveProperty = 2
        assertEquals(2, mock.primitiveProperty)
    }

    @Test
    fun testFieldValueIsSharedWithMock() {
        val field = field(1)
        every(mock::primitiveProperty) by field
        mock.primitiveProperty = 2
        assertEquals(2, field.value)
        field.value = 3
        assertEquals(3, mock.primitiveProperty)
    }

    @Test
    fun testFieldSharedBetweenMocks() {
        val other = mock<PropertiesInterface>(MockMode.strict)
        val field = field(1)
        every(mock::primitiveProperty) by field
        every(other::primitiveProperty) by field
        mock.primitiveProperty = 2
        assertEquals(2, other.primitiveProperty)
    }

    @Test
    fun testFieldCapturesOnlyMockWrites() {
        val writes = Capture.container<Int>()
        val field = field(1, writes)
        every(mock::primitiveProperty) by field
        mock.primitiveProperty = 2
        field.value = 3
        mock.primitiveProperty = 4
        assertEquals(4, mock.primitiveProperty)
        assertEquals(listOf(2, 4), writes.values)
    }

    @Test
    fun testFieldCapturesLastWriteWithSlot() {
        val lastWrite = Capture.slot<Int>()
        every(mock::primitiveProperty) by field(1, lastWrite)
        mock.primitiveProperty = 2
        mock.primitiveProperty = 3
        assertEquals(3, lastWrite.get())
    }

    @Test
    fun testBoundMutableProperty() {
        val holder = Holder()
        every(mock::primitiveProperty) by bound(holder::value)
        assertEquals(1, mock.primitiveProperty)
        mock.primitiveProperty = 2
        assertEquals(2, holder.value)
        holder.value = 3
        assertEquals(3, mock.primitiveProperty)
    }

    @Test
    fun testStdlibReadWriteDelegate() {
        var observed = -1
        every(mock::primitiveProperty) by Delegates.observable(1) { _, _, newValue -> observed = newValue }
        assertEquals(1, mock.primitiveProperty)
        mock.primitiveProperty = 5
        assertEquals(5, mock.primitiveProperty)
        assertEquals(5, observed)
    }

    @Test
    fun testStdlibDelegateReceivesMockedProperty() {
        val names = mutableListOf<String>()
        every(mock::primitiveProperty) by Delegates.vetoable(1) { property, _, _ ->
            names += property.name
            true
        }
        mock.primitiveProperty = 2
        assertEquals(listOf("primitiveProperty"), names)
    }

    @Test
    fun testStdlibReadWriteDelegateReceivesMockAsThisRef() {
        val thisRefs = mutableListOf<Any?>()
        every(mock::primitiveProperty) by object : ReadWriteProperty<Any?, Int> {
            override fun getValue(thisRef: Any?, property: KProperty<*>): Int {
                thisRefs += thisRef
                return 1
            }

            override fun setValue(thisRef: Any?, property: KProperty<*>, value: Int) {
                thisRefs += thisRef
            }
        }
        mock.primitiveProperty
        mock.primitiveProperty = 2
        assertEquals(listOf<Any?>(mock, mock), thisRefs)
    }

    @Test
    fun testStdlibLateinitDelegate() {
        every(mock::primitiveProperty) by Delegates.notNull()
        assertFailsWith<IllegalStateException> { mock.primitiveProperty }
        mock.primitiveProperty = 1
        assertEquals(1, mock.primitiveProperty)
    }

    @Test
    fun testReadOnlyReturns() {
        every(readOnlyMock::primitiveProperty) returns 1
        assertEquals(1, readOnlyMock.primitiveProperty)
    }

    @Test
    fun testReadOnlyInheritsFunctionAnswers() {
        every(readOnlyMock::primitiveProperty) sequentiallyReturns listOf(1, 2)
        assertEquals(1, readOnlyMock.primitiveProperty)
        assertEquals(2, readOnlyMock.primitiveProperty)
    }

    @Test
    fun testReadOnlyByField() {
        val field = field(1)
        every(readOnlyMock::primitiveProperty) by field
        field.value = 2
        assertEquals(2, readOnlyMock.primitiveProperty)
    }

    @Test
    fun testReadOnlyByBoundProperty() {
        val holder = Holder()
        every(readOnlyMock::primitiveProperty) by bound(holder::readOnly)
        holder.value = 2
        assertEquals(2, readOnlyMock.primitiveProperty)
    }

    @Test
    fun testReadOnlyStdlibDelegateReceivesMockAsThisRef() {
        var thisRef: Any? = null
        every(readOnlyMock::primitiveProperty) by ReadOnlyProperty { ref, _ ->
            thisRef = ref
            7
        }
        assertEquals(7, readOnlyMock.primitiveProperty)
        assertSame(readOnlyMock, thisRef)
    }

    @Test
    fun testVarWithInaccessibleSetterUsesReadOnlyScope() {
        val protectedSetterMock = mock<ProtectedSetterClass>(MockMode.strict)
        every(protectedSetterMock::value) returns 1
        assertEquals(1, protectedSetterMock.value)
    }

    private class Holder {
        var value = 1
        val readOnly: Int get() = value
    }
}

interface ReadOnlyPropertiesInterface {

    val primitiveProperty: Int
}

open class ProtectedSetterClass {

    open var value: Int = 0
        protected set
}
