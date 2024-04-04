package dev.mokkery.answering.autofill

import dev.mokkery.answering.autofill.AutofillProvider.Value
import dev.mokkery.internal.DefaultNothingException
import dev.mokkery.internal.unsafeCast
import kotlin.reflect.KClass
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class AutofillProviderForMockModeTest {

    private val providerForMock = AutofillProvider.forMockMode
    private val providerForInternals = AutofillProvider.forInternals
    private val newMockProvider = AutofillProvider { if (it == Int::class) Value.Provided(1) else Value.Absent }
    private val newInternalProvider = AutofillProvider { if (it == Int::class) Value.Provided(3) else Value.Absent }

    @BeforeTest
    fun before() {
        providerForMock.delegates.register(newMockProvider)
        providerForInternals.delegates.register(newInternalProvider)
        providerForMock.types.register { 2 }
        providerForMock.types.register { "Hello!" }
        providerForInternals.types.register { 4 }
        providerForInternals.types.register { 2.0 }
    }

    @AfterTest
    fun after() {
        providerForMock.delegates.unregister(newMockProvider)
        providerForInternals.delegates.unregister(newInternalProvider)
        providerForMock.types.unregister(Int::class)
        providerForMock.types.unregister(String::class)
        providerForInternals.types.unregister(Int::class)
        providerForInternals.types.unregister(Double::class)
    }

    @Test
    fun returnsFromForMockRegisteredDelegatesFirstAndThenFromOthers() {
        assertEquals(1, providerForMock.provideValue(Int::class))
        // others
        assertEquals("Hello!", providerForMock.provideValue(String::class))
        assertEquals(2.0, providerForMock.provideValue(Double::class))
        assertEquals(Any::class, providerForMock.provideValue(KClass::class))
    }

    @Test
    fun returnsFromForRegisteredTypesSecondAndThenFromOthers() {
        providerForMock.delegates.unregister(newMockProvider)
        assertEquals(2, providerForMock.provideValue(Int::class))
        // others
        assertEquals("Hello!", providerForMock.provideValue(String::class))
        assertEquals(2.0, providerForMock.provideValue(Double::class))
        assertEquals(Any::class, providerForMock.provideValue(KClass::class))
    }

    @Test
    fun returnsFromFoInternalsRegisteredDelegatesThird() {
        providerForMock.delegates.unregister(newMockProvider)
        providerForMock.types.unregister(Int::class)
        assertEquals(3, providerForMock.provideValue(Int::class))
        // others
        assertEquals("Hello!", providerForMock.provideValue(String::class))
        assertEquals(2.0, providerForMock.provideValue(Double::class))
        assertEquals(Any::class, providerForMock.provideValue(KClass::class))
    }

    @Test
    fun returnsFromForInternalsRegisteredTypesFourth() {
        providerForMock.delegates.unregister(newMockProvider)
        providerForMock.types.unregister(Int::class)
        providerForInternals.delegates.unregister(newInternalProvider)
        assertEquals(4, providerForMock.provideValue(Int::class))
        // others
        assertEquals("Hello!", providerForMock.provideValue(String::class))
        assertEquals(2.0, providerForMock.provideValue(Double::class))
        assertEquals(Any::class, providerForMock.provideValue(KClass::class))
    }

    @Test
    fun returnsCorrectDefaultsWhenNoAtLast() {
        // do clean up first to check defaults
        after()
        assertEquals(0, providerForMock.provideValue(Int::class))
        assertEquals("", providerForMock.provideValue(String::class))
        assertEquals(Any::class, providerForMock.provideValue(KClass::class))
        assertContentEquals(arrayOf<String?>(null), providerForMock.provideValue(arrayOf("")::class).unsafeCast())
        assertContentEquals(intArrayOf(0), providerForMock.provideValue(IntArray::class).unsafeCast())
        assertFailsWith<DefaultNothingException> { providerForMock.provideValue(Nothing::class) }
    }
}
