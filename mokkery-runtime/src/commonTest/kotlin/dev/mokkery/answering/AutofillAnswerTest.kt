package dev.mokkery.answering

import dev.mokkery.internal.DefaultNothingException
import dev.mokkery.internal.answering.autoFillMapping
import dev.mokkery.test.fakeFunctionScope
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull

class AutofillAnswerTest {

    private val answer = Answer.Autofill

    @Test
    fun testCallReturnsFromAutofillMapping() {
        autoFillMapping.forEach { (type, value) ->
            assertEquals(value, answer.call(fakeFunctionScope(type)))
        }
    }

    @Test
    fun testCallThrowsNothingOnNothingClass() {
        assertFailsWith<DefaultNothingException> {
            answer.call(fakeFunctionScope(Nothing::class))
        }
    }


    @Test
    fun testCallReturnsNullOnComplexType() {
        assertNull(answer.call(fakeFunctionScope(List::class)))
    }

    @Test
    fun testCallSuspendReturnsFromAutofillMapping() = runTest {
        autoFillMapping.forEach { (type, value) ->
            assertEquals(value, answer.callSuspend(fakeFunctionScope(type)))
        }
    }

    @Test
    fun testCallSuspendThrowsNothingOnNothingClass() = runTest {
        assertFailsWith<DefaultNothingException> {
            answer.callSuspend(fakeFunctionScope(Nothing::class))
        }
    }

    @Test
    fun testCallSuspendReturnsNullOnComplexType() = runTest {
        assertNull(answer.callSuspend(fakeFunctionScope(List::class)))
    }

}
