package dev.mokkery.answering.autofill

import dev.mokkery.answering.autofill.AutofillProvider.Value
import dev.mokkery.internal.DefaultNothingException
import dev.mokkery.internal.unsafeCast
import kotlin.reflect.KClass
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class AutofillProviderCompanionTest {

    private val companion = AutofillProvider
    private val newProvider = AutofillProvider { if (it == Int::class) Value.Provided(37) else Value.Absent }

    @AfterTest
    fun afterTest() {
        companion.unregisterProvider(newProvider)
        companion.unregister<Int>()
    }

    @Test
    fun testProvidesCorrectDefaults() {
        checkDefaults(companion)
    }

    @Test
    fun testProvidesCorrectDefaultsForBuiltIn() {
        checkDefaults(companion.builtIn)
    }

    @Test
    fun testUsesRegisteredProvidersFirst() {
        companion.registerProvider(newProvider)
        assertEquals(37, companion.provideValue(Int::class))
    }

    @Test
    fun testUsesDefaultsWhenRegisteredProvidedReturnsAbsent() {
        companion.registerProvider(newProvider)
        assertEquals(0.toByte(), companion.provideValue(Byte::class))
    }

    @Test
    fun testUnregisteredProviderDoesNotAffectResult() {
        companion.registerProvider(newProvider)
        companion.unregisterProvider(newProvider)
        assertEquals(0, companion.provideValue(Int::class))
    }

    @Test
    fun testUsesRegisteredProviderForRegisteredType() {
        companion.register { 21 }
        assertEquals(21, companion.provideValue(Int::class))
    }


    @Test
    fun testOverwritesProviderForRegisteredType() {
        companion.register { 21 }
        companion.register { 23 }
        assertEquals(23, companion.provideValue(Int::class))
    }

    @Test
    fun testUsesDefaultsWhenProviderForSpecificTypeIsNotRegistered() {
        companion.register { 21 }
        assertEquals(0.toByte(), companion.provideValue(Byte::class))
    }

    @Test
    fun testUnregisteredProviderForSpecificTypeDoesNotAffectResult() {
        companion.register { 21 }
        companion.unregister(Int::class)
        assertEquals(0, companion.provideValue(Int::class))
    }

    @Test
    fun testUsesProvidersInCorrectOrder() {
        companion.registerProvider(newProvider)
        assertEquals(37, companion.provideValue(Int::class))
        companion.register { 21 }
        assertEquals(37, companion.provideValue(Int::class))
        companion.unregisterProvider(newProvider)
        assertEquals(21, companion.provideValue(Int::class))
        companion.unregister(Int::class)
        assertEquals(0, companion.provideValue(Int::class))
    }

    private fun checkDefaults(provider: AutofillProvider<Any?>) {
        assertEquals(0, provider.provideValue(Int::class))
        assertEquals("", provider.provideValue(String::class))
        assertEquals(Any::class, provider.provideValue(KClass::class))
        assertContentEquals(arrayOf<String?>(null), provider.provideValue(arrayOf("")::class).unsafeCast())
        assertContentEquals(intArrayOf(0), provider.provideValue(IntArray::class).unsafeCast())
        assertFailsWith<DefaultNothingException> { provider.provideValue(Nothing::class) }
    }
}
