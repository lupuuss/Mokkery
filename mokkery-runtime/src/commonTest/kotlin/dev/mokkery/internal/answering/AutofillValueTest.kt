package dev.mokkery.internal.answering

import dev.mokkery.internal.DefaultNothingException
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull

class AutofillValueTest {

    @Test
    fun testCallReturnsFromAutofillMapping() {
        autofillMapping.forEach { (type, value) ->
            assertEquals(value, autofillValue(type))
        }
    }

    @Test
    fun testCallThrowsNothingOnNothingClass() {
        assertFailsWith<DefaultNothingException> {
            autofillValue(Nothing::class)
        }
    }


    @Test
    fun testCallReturnsNullOnComplexType() {
        assertNull(autofillValue((List::class)))
    }

}
