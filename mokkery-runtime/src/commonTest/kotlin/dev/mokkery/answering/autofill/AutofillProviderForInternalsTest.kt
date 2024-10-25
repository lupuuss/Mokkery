package dev.mokkery.answering.autofill

import dev.mokkery.internal.DefaultNothingException
import dev.mokkery.internal.utils.unsafeCast
import kotlin.reflect.KClass
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class AutofillProviderForInternalsTest {

    private val provider = AutofillProvider.forInternals
    private val newProvider = AutofillProvider.ofNotNull { if (it == Int::class) 37 else null }

    @BeforeTest
    fun before() {
        provider.delegates.register(newProvider)
        provider.types.register { 11 }
        provider.types.register { "Hello!" }
    }

    @AfterTest
    fun after() {
        provider.delegates.unregister(newProvider)
        provider.types.unregister(Int::class)
        provider.types.unregister(String::class)
    }

    @Test
    fun returnsFromRegisteredDelegatesFirst() {
        assertEquals(37, provider.provideValue(Int::class))
        assertEquals("Hello!", provider.provideValue(String::class))
        assertEquals(Any::class, provider.provideValue(KClass::class))
    }

    @Test
    fun returnsFromRegisteredTypesSecond() {
        provider.delegates.unregister(newProvider)
        assertEquals(11, provider.provideValue(Int::class))
        assertEquals("Hello!", provider.provideValue(String::class))
        assertEquals(Any::class, provider.provideValue(KClass::class))
    }

    @Test
    fun returnsCorrectDefaultsWhenNoAtLast() {
        // do clean up first to check defaults
        after()
        assertEquals(0, provider.provideValue(Int::class))
        assertEquals("", provider.provideValue(String::class))
        assertEquals(Any::class, provider.provideValue(KClass::class))
        assertContentEquals(arrayOf<String?>(null), provider.provideValue(arrayOf("")::class).unsafeCast())
        assertContentEquals(intArrayOf(0), provider.provideValue(IntArray::class).unsafeCast())
        assertFailsWith<DefaultNothingException> { provider.provideValue(Nothing::class) }
    }
}
