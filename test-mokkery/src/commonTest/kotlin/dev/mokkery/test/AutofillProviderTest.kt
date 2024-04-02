package dev.mokkery.test

import dev.mokkery.MockMode.autofill
import dev.mokkery.mock
import dev.mokkery.answering.autofill.AutofillProvider
import dev.mokkery.answering.autofill.register
import dev.mokkery.answering.autofill.unregister
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

class AutofillProviderTest {

    private val mock = mock<TestInterface>(autofill)
    private val customProvider = AutofillProvider {
        AutofillProvider.Value.providedIfNotNull { if (it == String::class) "Hello!" else null }
    }

    @BeforeTest
    fun before() {
        AutofillProvider.registerProvider(customProvider)
        AutofillProvider.register<Int> { 7312 }
    }

    @AfterTest
    fun after() {
        AutofillProvider.unregisterProvider(customProvider)
        AutofillProvider.unregister<Int>()
    }

    @Test
    fun testProvidesAutofillValuesFromProvider() {
        assertEquals(7312, mock.callWithDefault(0))
        assertEquals("Hello!", mock.callWithIntArray(intArrayOf()))
    }

    @Test
    fun testProperlyUnregistersProvider() {
        AutofillProvider.unregisterProvider(customProvider)
        AutofillProvider.unregister<Int>()
        assertEquals(0, mock.callWithDefault(0))
        assertEquals("", mock.callWithIntArray(intArrayOf()))
    }
}
