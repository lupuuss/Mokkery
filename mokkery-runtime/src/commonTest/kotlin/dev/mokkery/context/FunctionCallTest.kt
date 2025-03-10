package dev.mokkery.context

import dev.mokkery.test.fakeCallArg
import dev.mokkery.test.fakeFunctionCall
import kotlin.test.Test
import kotlin.test.assertEquals

class FunctionCallTest {

    private val functionCall = fakeFunctionCall(args = listOf(fakeCallArg(1), fakeCallArg("3")))
    
    @Test
    fun testArgValueReturnsCastedArgumentValue() {
        assertEquals(1, functionCall.argValue(0))
        assertEquals("3", functionCall.argValue(1))
    }
}
